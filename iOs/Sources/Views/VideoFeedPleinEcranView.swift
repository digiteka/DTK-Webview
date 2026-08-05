import SwiftUI
import VideoFeedSDK

struct VideoFeedPleinEcranView: View {
    @AppStorage("videoFeedMdtk") private var mdtk = "01573101"
    @AppStorage("videoFeedVideoId") private var videoId = ""

    var body: some View {
        Group {
            if videoId.isEmpty {
                VideoFeedViewSUI(mdtk: mdtk)
            } else {
                VideoFeedViewSUI(videoId: videoId, mdtk: mdtk)
            }
        }
        .navigationTitle("VideoFeed Plein Ecran")
        .navigationBarTitleDisplayMode(.inline)
    }
}
