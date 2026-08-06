import SwiftUI

struct ContentView: View {
    var body: some View {
        // NavigationView + style .stack : NavigationStack exige iOS 16.
        NavigationView {
            List {
                Section {
                    NavigationLink(destination: InstreamView()) {
                        ScenarioRow(
                            icon: "newspaper",
                            title: "Player sans SDK",
                            subtitle: "Player chargé dans une webview Ultimedia"
                        )
                    }

                    NavigationLink(destination: ConfigInstreamView()) {
                        ScenarioRow(
                            icon: "gearshape",
                            title: "Configuration Player",
                            subtitle: "Paramètres MDTK, Zone, SRC, Referer..."
                        )
                    }

                    NavigationLink(destination: CookieManagerView()) {
                        ScenarioRow(
                            icon: "gearshape",
                            title: "Configuration Cookie",
                            subtitle: "Gestion des cookies ultimedia.com"
                        )
                    }
                }

                Section {
                    NavigationLink(destination: VideoFeedCarrouselView()) {
                        ScenarioRow(
                            icon: "newspaper",
                            title: "VideoFeed Carrousel",
                            subtitle: "Carrousel intégré dans une page"
                        )
                    }

                    NavigationLink(destination: VideoFeedPleinEcranView()) {
                        ScenarioRow(
                            icon: "arrow.up.left.and.arrow.down.right",
                            title: "VideoFeed Plein Ecran",
                            subtitle: "Feed vidéo en plein écran"
                        )
                    }

                    NavigationLink(destination: ConfigVideoFeedView()) {
                        ScenarioRow(
                            icon: "gearshape",
                            title: "Configuration VideoFeed",
                            subtitle: "Paramètres du feed vidéo"
                        )
                    }
                }
            }
            .navigationTitle("DTK Tester")
        }
        // Évite le split view sur iPad, comportement proche de NavigationStack.
        .navigationViewStyle(.stack)
    }
}

// MARK: - Row

private struct ScenarioRow: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundStyle(.blue)
                .frame(width: 36)
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}
