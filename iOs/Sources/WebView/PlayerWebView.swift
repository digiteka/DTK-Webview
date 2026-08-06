import SwiftUI
import WebKit
import os.log

/// Permet de déclencher des actions sur la WKWebView sans la détruire.
class WebViewAction: ObservableObject {
    var reload: (() -> Void)?
}

struct PlayerWebView: UIViewRepresentable {
    let html: String
    let baseURL: String
    let viewModel: PlayerViewModel
    let action: WebViewAction

    func makeCoordinator() -> Coordinator {
        Coordinator(viewModel: viewModel, action: action)
    }

    final class KeyCommandWebView: WKWebView {
        weak var coordinator: Coordinator?

        override var keyCommands: [UIKeyCommand]? {
            let inherited = (super.keyCommands ?? []).filter {
                !($0.input == "r" && $0.modifierFlags.contains(.command))
            }
            let reload = UIKeyCommand(
                title: "Recharger le player",
                action: #selector(handleReloadShortcut),
                input: "r",
                modifierFlags: .command
            )
            return inherited + [reload]
        }

        @objc private func handleReloadShortcut() {
            coordinator?.action.reload?()
        }
    }

    func makeUIView(context: Context) -> WKWebView {
        // ── Configuration ────────────────────────────────────────────────────
        let controller = WKUserContentController()
        controller.add(context.coordinator, name: "playerEvents")
        controller.add(context.coordinator, name: "consoleLogs")

        let config = WKWebViewConfiguration()
        config.userContentController = controller

        // Lecture des médias sans interaction utilisateur (fondamental pour l'autoplay)
        config.mediaTypesRequiringUserActionForPlayback = []

        // Lecture inline sans basculer en plein écran automatiquement
        config.allowsInlineMediaPlayback = true

        // Nécessaire pour les pubs VPAID, les clics pub et les boutons externes
        config.preferences.javaScriptCanOpenWindowsAutomatically = true

        // ── WebView ──────────────────────────────────────────────────────────
        let webView = KeyCommandWebView(frame: .zero, configuration: config)
        webView.coordinator = context.coordinator
        webView.navigationDelegate = context.coordinator
        webView.uiDelegate = context.coordinator
        webView.scrollView.bounces = true

        // Activation du Web Inspector Safari (iOS 16.4+)
        if #available(iOS 16.4, *) {
            webView.isInspectable = true
        }

        // Enregistre la référence et expose l'action de reload
        // On recharge le HTML directement plutôt que webView.reload() qui navigue vers la baseURL
        context.coordinator.webView = webView
        context.coordinator.action.reload = { [weak webView, weak coordinator = context.coordinator] in
            guard let webView, let coordinator else { return }
            let base = URL(string: coordinator.lastBaseURL) ?? URL(string: "https://www.ultimedia.com")
            webView.loadHTMLString(coordinator.lastHTML, baseURL: base)
        }

        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        guard context.coordinator.lastHTML != html else { return }
        context.coordinator.lastHTML = html
        context.coordinator.lastBaseURL = baseURL
        let base = URL(string: baseURL) ?? URL(string: "https://www.ultimedia.com")
        webView.loadHTMLString(html, baseURL: base)
    }

    // MARK: - Coordinator

    class Coordinator: NSObject, WKScriptMessageHandler, WKNavigationDelegate, WKUIDelegate {
        let viewModel: PlayerViewModel
        let action: WebViewAction
        var lastHTML: String = ""
        var lastBaseURL: String = ""
        weak var webView: WKWebView?

        private let consoleLogger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "DTKTester", category: "WebConsole")

        init(viewModel: PlayerViewModel, action: WebViewAction) {
            self.viewModel = viewModel
            self.action = action
        }

        // MARK: WKScriptMessageHandler

        func userContentController(
            _ userContentController: WKUserContentController,
            didReceive message: WKScriptMessage
        ) {
            switch message.name {

            case "playerEvents":
                guard let body = message.body as? [String: Any],
                      let event = body["event"] as? String
                else { return }
                viewModel.addEvent(event)

            case "consoleLogs":
                guard let body = message.body as? [String: Any],
                      let level = body["level"] as? String,
                      let text = body["message"] as? String
                else { return }

                // Log natif — visible dans Console.app et survit au crash/reload de la WKWebView
                switch level {
                case "error": consoleLogger.error("🔴 \(text, privacy: .public)")
                case "warn":  consoleLogger.warning("🟡 \(text, privacy: .public)")
                case "info":  consoleLogger.info("🔵 \(text, privacy: .public)")
                default:      consoleLogger.debug("⚪ \(text, privacy: .public)")
                }

                // Remonte aussi dans l'UI in-app pour un accès sans Console.app
                let prefix: String
                switch level {
                case "error": prefix = "[console.error]"
                case "warn":  prefix = "[console.warn]"
                case "info":  prefix = "[console.info]"
                default:      prefix = "[console.log]"
                }
                viewModel.addConsoleLog("\(prefix) \(text)")

            default:
                break
            }
        }

        // MARK: WKNavigationDelegate

        /// Autorise toutes les navigations — nécessaire pour les clics pub
        public func webView(
            _ webView: WKWebView,
            decidePolicyFor navigationAction: WKNavigationAction,
            decisionHandler: @escaping (WKNavigationActionPolicy) -> Void
        ) {
            decisionHandler(.allow)
        }

        /// Accepte les certificats auto-signés du serveur de dev local
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

        func webView(
            _ webView: WKWebView,
            didFailProvisionalNavigation navigation: WKNavigation!,
            withError error: Error
        ) {
            viewModel.addEvent("⚠️ Erreur navigation: \(error.localizedDescription)")
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            viewModel.addEvent("✅ Page chargée")
        }

        // MARK: WKUIDelegate

        /// Gère les window.open() du player (clics pub, liens externes)
        /// Ouvre les URL http/https dans Safari, ignore le reste
        public func webView(
            _ webView: WKWebView,
            createWebViewWith configuration: WKWebViewConfiguration,
            for navigationAction: WKNavigationAction,
            windowFeatures: WKWindowFeatures
        ) -> WKWebView? {
            guard let url = navigationAction.request.url,
                  let scheme = url.scheme,
                  scheme == "http" || scheme == "https"
            else { return nil }

            UIApplication.shared.open(url, options: [:], completionHandler: nil)
            return nil
        }
    }
}
