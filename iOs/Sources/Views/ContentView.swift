import SwiftUI

struct ContentView: View {
    var body: some View {
        // NavigationView + style .stack : NavigationStack exige iOS 16.
        NavigationView {
            List {
                NavigationLink(destination: InstreamView()) {
                    ScenarioRow(
                        icon: "newspaper",
                        title: "Instream sans SDK",
                        subtitle: "Player intégré dans un article Lorem Ipsum"
                    )
                }

                NavigationLink(destination: ConfigInstreamView()) {
                    ScenarioRow(
                        icon: "slider.horizontal.3",
                        title: "Config Instream",
                        subtitle: "Paramètres MDTK, Zone, SRC, Referer"
                    )
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
