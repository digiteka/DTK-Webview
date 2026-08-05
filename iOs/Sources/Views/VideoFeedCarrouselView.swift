import SwiftUI

struct VideoFeedCarrouselView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "square.stack.3d.up")
                .font(.largeTitle)
                .foregroundStyle(.secondary)
            Text("VideoFeed Carrousel")
                .font(.headline)
            Text("Écran à venir")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .navigationTitle("VideoFeed Carrousel")
        .navigationBarTitleDisplayMode(.inline)
    }
}
