import SwiftUI

struct InstreamView: View {
    @StateObject private var viewModel = PlayerViewModel()
    @StateObject private var webViewAction = WebViewAction()
    @AppStorage("mdtk") private var mdtk = "01211820"
    @AppStorage("src") private var src = "3v83mr3"
    @AppStorage("zone") private var zone = "3"
    @AppStorage("autoplay") private var autoplay = 1
    @AppStorage("sound") private var sound = 1
    @AppStorage("ad") private var ad = 1
    @AppStorage("refererURL") private var refererURL = "https://www.digiteka.com"
    @AppStorage("consentStringEnabled") private var consentStringEnabled = true
    @AppStorage("newplayerMode") private var newplayerModeRaw = NewplayerMode.legacy.rawValue
    @AppStorage("newplayerBranchName") private var newplayerBranchName = ""
    @AppStorage("newplayerLocalIP") private var newplayerLocalIP = ""
    @AppStorage("tagParam") private var tagParam = ""
    @State private var showLogs = false
    @State private var reloadToken = UUID()

    private var consentString: String {
        consentStringEnabled ? ConfigInstreamView.defaultConsentString : ""
    }

    private var newplayer: String? {
        (NewplayerMode(rawValue: newplayerModeRaw) ?? .legacy)
            .resolvedValue(branchName: newplayerBranchName, localIP: newplayerLocalIP)
    }

    private var html: String {
        HTMLGenerator.generateInstream(
            mdtk: mdtk,
            zone: zone,
            src: src,
            autoplay: autoplay,
            sound: sound,
            ad: ad,
            refererURL: refererURL,
            consentString: consentString,
            newplayer: newplayer,
            tagParam: tagParam.isEmpty ? nil : tagParam
        )
    }

    var body: some View {
        VStack(spacing: 0) {
            PlayerWebView(html: html, baseURL: refererURL, viewModel: viewModel, action: webViewAction)
                .id(reloadToken)

            // Barre de contrôle
            HStack(spacing: 8) {
                Image(systemName: "dot.radiowaves.left.and.right")
                    .foregroundColor(viewModel.logs.isEmpty ? .secondary : .green)
                Text("\(viewModel.logs.count) événement\(viewModel.logs.count > 1 ? "s" : "")")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Spacer()
                Button { viewModel.clear() } label: {
                    Image(systemName: "trash")
                }
                .buttonStyle(.bordered)
                .controlSize(.small)

                // Refresh sans détruire la webview — préserve la session Web Inspector Safari
                Button {
                    webViewAction.reload?()
                } label: {
                    Image(systemName: "arrow.triangle.2.circlepath")
                }
                .buttonStyle(.bordered)
                .controlSize(.small)

                Button {
                    viewModel.clear()
                    reloadToken = UUID()
                } label: {
                    Image(systemName: "arrow.clockwise")
                }
                .buttonStyle(.bordered)
                .controlSize(.small)

                Button { withAnimation { showLogs.toggle() } } label: {
                    Label(
                        showLogs ? "Fermer" : "Logs",
                        systemImage: showLogs ? "chevron.down" : "list.bullet.rectangle"
                    )
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.small)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(.bar)

            if showLogs {
                LogListView(logs: viewModel.logs)
                    .frame(maxHeight: 280)
                    .transition(.move(edge: .bottom))
            }
        }
        .navigationTitle("Instream sans SDK")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Log list

struct LogListView: View {
    let logs: [EventLog]

    var body: some View {
        if logs.isEmpty {
            VStack(spacing: 12) {
                Image(systemName: "antenna.radiowaves.left.and.right.slash")
                    .font(.largeTitle)
                    .foregroundStyle(.secondary)
                Text("Aucun événement")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text("Les événements du player apparaîtront ici")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            List(logs) { log in
                HStack(alignment: .top, spacing: 8) {
                    Text(log.category.emoji)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(log.rawEvent)
                            .font(.caption.monospaced())
                            .lineLimit(3)
                        Text(log.formattedTime)
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                    }
                }
            }
            .listStyle(.plain)
        }
    }
}
