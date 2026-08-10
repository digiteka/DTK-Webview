import SwiftUI

struct VideoFeedFullscreenView: View {
    @AppStorage("videoFeedMdtk") private var mdtk = "01412408"
    @AppStorage("videoFeedVideoId") private var videoId = ""
    @AppStorage("videoFeedVfBranch") private var vfBranch = ""

    var body: some View {
        Group {
            if let url = HTMLGenerator.videoFeedFullscreenURL(
                mdtk: mdtk,
                videoId: videoId.isEmpty ? nil : videoId,
                zoneId: nil,
                vfBranch: vfBranch.isEmpty ? nil : vfBranch,
                consentString: UserDefaults.standard.string(forKey: "IABTCF_TCString") ?? ""
            ) {
                VideoFeedFullscreenWebView(url: url)
            }
        }
        .navigationTitle("VideoFeed Plein Ecran")
        .navigationBarTitleDisplayMode(.inline)
    }
}
