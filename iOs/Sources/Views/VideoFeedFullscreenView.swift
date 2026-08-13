import SwiftUI

struct VideoFeedFullscreenView: View {
    @AppStorage("videoFeedMdtk") private var mdtk = "01412408"
    @AppStorage("videoFeedVideoId") private var videoId = ""
    @AppStorage("videoFeedVfBranch") private var vfBranch = ""
    @AppStorage("videoFeedConsentStringEnabled") private var consentStringEnabled = true

    private var debugConsentString: String? {
        consentStringEnabled ? ConfigVideoFeedView.defaultConsentString : nil
    }

    var body: some View {
        Group {
            if let url = HTMLGenerator.videoFeedFullscreenURL(
                mdtk: mdtk,
                videoId: videoId.isEmpty ? nil : videoId,
                zoneId: nil,
                vfBranch: vfBranch.isEmpty ? nil : vfBranch,
                consentString: debugConsentString ?? ""
            ) {
                VideoFeedFullscreenWebView(url: url, consentString: debugConsentString)
            }
        }
        .navigationTitle("VideoFeed Plein Ecran")
        .navigationBarTitleDisplayMode(.inline)
    }
}
