import SwiftUI
import VideoFeedSDK
import WebKit

struct VideoFeedCarrouselView: View {
    @AppStorage("videoFeedMdtk") private var mdtk = "01573101"
    @AppStorage("videoFeedZoneId") private var zoneId = 0
    @AppStorage("videoFeedAdunitPath") private var adunitPath = ""
    @AppStorage("videoFeedVfBranch") private var vfBranch = ""
    @AppStorage("videoFeedCarrBranch") private var carrBranch = ""
    @State private var fullScreenVideoId: String?
    @State private var fullScreenZoneId: Int?
    @State private var showFullScreen = false

    private var html: String {
        HTMLGenerator.generateVideoFeedCarrousel(
            mdtk: mdtk,
            consentString: UserDefaults.standard.string(forKey: "IABTCF_TCString") ?? "",
            adUnitPath: adunitPath.isEmpty ? nil : adunitPath,
            zoneId: zoneId == 0 ? nil : zoneId,
            vfBranch: vfBranch.isEmpty ? nil : vfBranch,
            carrBranch: carrBranch.isEmpty ? nil : carrBranch
        )
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                CarrouselWebView(html: html, host: HTMLGenerator.videoFeedHost(vfBranch: vfBranch.isEmpty ? nil : vfBranch)) { tappedMdtk, videoId, tappedZoneId in
                    mdtk = tappedMdtk
                    fullScreenVideoId = videoId
                    fullScreenZoneId = tappedZoneId
                    showFullScreen = true
                }
                .frame(height: 280)
                .background(Color.black)

                Text("Intégration VideoFeed")
                    .font(.title3.bold())
                    .padding(.horizontal, 16)
                    .padding(.top, 24)
                    .padding(.bottom, 8)

                Text(Self.loremIpsum)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
            }
        }
        .navigationTitle("VideoFeed Carrousel")
        .navigationBarTitleDisplayMode(.inline)
        .fullScreenCover(isPresented: $showFullScreen) {
            VideoFeedViewSUI(
                videoId: fullScreenVideoId,
                zoneId: fullScreenZoneId,
                mdtk: mdtk,
                adUnitPath: adunitPath.isEmpty ? nil : adunitPath,
                showCloseButton: true
            )
        }
    }

    private static let loremIpsum = """
    Le composant VideoFeed Carrousel permet d'intégrer facilement un carrousel de vidéos dans n'importe quelle page de votre application.

    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

    Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    """
}

// MARK: - Raw WKWebView (contourne VideoFeedCarrouselViewSUI pour piloter les globals window.MDTK_*)

private struct CarrouselWebView: UIViewRepresentable {
    let html: String
    /// Host servant launcher.min.js (videofeed.digiteka.com, ou la preview Amplify si VF_BRANCH est renseigné) —
    /// utilisé comme baseURL et comme host attendu lors de l'interception d'un tap.
    let host: String
    /// (mdtk, videoId, zoneId) extraits de la navigation interceptée — reproduit l'interception faite côté
    /// Android par VideoFeedCarousel$WebViewClient (host videofeed.digiteka.com + query mdtk), puisque la
    /// webview brute ne bénéficie plus du WebViewClient interne (fermé) du SDK.
    let onVideoTap: (String, String?, Int?) -> Void

    private var baseURL: URL? { URL(string: "https://\(host)") }

    func makeCoordinator() -> Coordinator {
        Coordinator(host: host, onVideoTap: onVideoTap)
    }

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView(frame: .zero)
        webView.navigationDelegate = context.coordinator
        webView.scrollView.isScrollEnabled = false
        webView.isOpaque = false
        webView.backgroundColor = .clear
        if #available(iOS 16.4, *) {
            webView.isInspectable = true
        }
        context.coordinator.lastHTML = html
        webView.loadHTMLString(html, baseURL: baseURL)
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        context.coordinator.host = host
        guard context.coordinator.lastHTML != html else { return }
        context.coordinator.lastHTML = html
        webView.loadHTMLString(html, baseURL: baseURL)
    }

    static func dismantleUIView(_ webView: WKWebView, coordinator: Coordinator) {
        webView.navigationDelegate = nil
        webView.stopLoading()
        webView.loadHTMLString("", baseURL: nil)
    }

    class Coordinator: NSObject, WKNavigationDelegate {
        var host: String
        let onVideoTap: (String, String?, Int?) -> Void
        var lastHTML = ""

        init(host: String, onVideoTap: @escaping (String, String?, Int?) -> Void) {
            self.host = host
            self.onVideoTap = onVideoTap
        }

        func webView(
            _ webView: WKWebView,
            decidePolicyFor navigationAction: WKNavigationAction,
            decisionHandler: @escaping (WKNavigationActionPolicy) -> Void
        ) {
            guard let url = navigationAction.request.url,
                  let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
                  components.host == host,
                  let mdtk = components.queryItems?.first(where: { $0.name == "mdtk" })?.value
            else {
                decisionHandler(.allow)
                return
            }

            let videoId = components.queryItems?.first(where: { $0.name == "video_id" })?.value
            let zoneId = components.queryItems?.first(where: { $0.name == "vf_zone_index" })?.value
                .flatMap(Int.init)
            onVideoTap(mdtk, videoId, zoneId)
            decisionHandler(.cancel)
        }
    }
}
