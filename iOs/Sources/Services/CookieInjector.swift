import UIKit
import WebKit

/// `HTTPCookie(properties:)` ignore silencieusement toute valeur de `.sameSitePolicy` autre que
/// `.sameSiteLax`/`.sameSiteStrict` (aucun cas "None" — vérifié : ni chaîne brute ni
/// `HTTPCookieStringPolicy(rawValue: "None")` ne survivent à l'init, la clé disparaît des
/// `properties` du cookie créé). Donc pour un cookie SameSite=None on passe par `document.cookie`,
/// exécuté dans une page chargée sur le domaine cible — seul chemin qui accepte l'attribut.
final class CookieInjector: NSObject, WKNavigationDelegate {
    typealias Completion = (Result<Void, Error>) -> Void

    enum InjectorError: LocalizedError {
        case invalidURL
        case timeout

        var errorDescription: String? {
            switch self {
            case .invalidURL: return "URL invalide"
            case .timeout: return "Délai dépassé (10s) — aucune réponse de la page cible"
            }
        }
    }

    private static var activeInstances: [CookieInjector] = []

    private var webView: WKWebView?
    private var script = ""
    private var completion: Completion?
    private var isFinished = false

    static func setCookie(_ setCookieString: String, onPage urlString: String, completion: @escaping Completion) {
        guard let url = URL(string: urlString) else {
            completion(.failure(InjectorError.invalidURL))
            return
        }

        let injector = CookieInjector()
        activeInstances.append(injector)
        injector.script = "document.cookie = \(setCookieString.debugDescription);"
        injector.completion = { result in
            completion(result)
            activeInstances.removeAll { $0 === injector }
        }

        // Une WKWebView jamais rattachée à une fenêtre ne déclenche pas ses callbacks de navigation
        // de façon fiable — on l'attache donc en 1x1 masquée plutôt qu'en frame .zero détachée.
        let webView = WKWebView(frame: CGRect(x: 0, y: 0, width: 1, height: 1))
        webView.isHidden = true
        injector.webView = webView
        webView.navigationDelegate = injector
        hostWindow?.addSubview(webView)
        webView.load(URLRequest(url: url))

        // Timeout explicite : sans lui, un hang réseau ne produit aucun signal (succès ni échec).
        DispatchQueue.main.asyncAfter(deadline: .now() + 10) {
            injector.finish(.failure(InjectorError.timeout))
        }
    }

    private static var hostWindow: UIWindow? {
        let windows = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
        return windows.first { $0.isKeyWindow } ?? windows.first
    }

    private func finish(_ result: Result<Void, Error>) {
        guard !isFinished else { return }
        isFinished = true
        completion?(result)
        completion = nil
        webView?.removeFromSuperview()
        webView = nil
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        webView.evaluateJavaScript(script) { [weak self] _, error in
            if let error {
                self?.finish(.failure(error))
            } else {
                self?.finish(.success(()))
            }
        }
    }

    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        finish(.failure(error))
    }

    /// Même tolérance que PlayerWebView.Coordinator — nécessaire pour les mêmes environnements
    /// de recette/dev à certificat non standard.
    func webView(
        _ webView: WKWebView,
        didReceive challenge: URLAuthenticationChallenge,
        completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void
    ) {
        if let serverTrust = challenge.protectionSpace.serverTrust {
            completionHandler(.useCredential, URLCredential(trust: serverTrust))
        } else {
            completionHandler(.performDefaultHandling, nil)
        }
    }

    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        finish(.failure(error))
    }
}
