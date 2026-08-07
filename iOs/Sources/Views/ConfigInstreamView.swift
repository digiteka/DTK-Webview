import SwiftUI
import UIKit

struct ConfigInstreamView: View {
    static let defaultConsentString = "CQlo8kAQlo8kAAHABBENCiFsAP_gAEPgAAAALAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABBYAgPYX7iKQsTiQI_BdkASGL2CkAVBgEIIBBAASAMQBABJGAWswgRAEQAAQBAAIABASIAAAEIAAIAIAAABAAJCIQgAgCACIAABQIAAACKEAAAAEIASAAAwACEBhKgAAgEAAggANUCAsAACAEEUAABAAAoBAQgAAAAAAECAAABAAAAEyAAkAADgBAAgAAAAIhgAAAFAAAQAAAAgACAAAACAAAAAAAAAAEAAABEACAAAAAAIAwSADAAEGSB0AGAAIMkEIAMAAQZIJQAYAAgyQUgAwABBkgtABgACDJAAA.ILAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABAA.f_wAH_wAAAAA"

    @State private var copiedURL = false
    @AppStorage("mdtk") private var mdtk = "01211820"
    @AppStorage("src") private var src = "3v83mr3"
    @AppStorage("zone") private var zone = "3"
    @AppStorage("refererURL") private var refererURL = "https://www.ultimedia.com"
    @AppStorage("consentStringEnabled") private var consentStringEnabled = true
    @AppStorage("newplayerMode") private var newplayerModeRaw = NewplayerMode.legacy.rawValue
    @AppStorage("newplayerBranchName") private var newplayerBranchName = ""
    @AppStorage("newplayerLocalIP") private var newplayerLocalIP = ""
    @AppStorage("tagParam") private var tagParam = ""
    @AppStorage("playerURLOverride") private var playerURLOverride = ""

    private var consentString: String {
        consentStringEnabled ? Self.defaultConsentString : ""
    }

    private var newplayerMode: NewplayerMode {
        NewplayerMode(rawValue: newplayerModeRaw) ?? .legacy
    }

    private var newplayerModeBinding: Binding<NewplayerMode> {
        Binding(
            get: { newplayerMode },
            set: { newplayerModeRaw = $0.rawValue }
        )
    }

    private var newplayer: String? {
        newplayerMode.resolvedValue(branchName: newplayerBranchName, localIP: newplayerLocalIP)
    }

    private var iframeURLPreview: String {
        HTMLGenerator.iframeURL(
            mdtk: mdtk.isEmpty ? "[MDTK]" : mdtk,
            zone: zone.isEmpty ? "[Zone]" : zone,
            src: src.isEmpty ? "[SRC]" : src,
            autoplay: 1,
            sound: 1,
            ad: 1,
            newplayer: newplayer,
            refererURL: refererURL,
            tagParam: tagParam.isEmpty ? nil : tagParam,
            consentString: consentString,
            override: playerURLOverride
        )
    }

    var body: some View {
        Form {
            // ── Champs de configuration ──────────────────────────────────────
            Section {
                LabeledRow("MDTK") {
                    TextField("ex: 01xxxxxx", text: $mdtk)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }
                LabeledRow("Zone") {
                    TextField("ex: 3", text: $zone)
                        .multilineTextAlignment(.trailing)
                        .keyboardType(.numberPad)
                }
                LabeledRow("ID Vidéo") {
                    TextField("ex: 3v83mr3", text: $src)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }
                VStack(alignment: .leading, spacing: 4) {
                    Text("URL referrer")
                        .font(.subheadline)
                    TextField("facultatif — ex: https://monsite.com", text: $refererURL)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .keyboardType(.URL)
                        .foregroundStyle(refererURL.isEmpty ? .secondary : .primary)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Tag Param")
                        .font(.subheadline)
                    TextField("facultatif", text: $tagParam)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .foregroundStyle(tagParam.isEmpty ? .secondary : .primary)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Consent String")
                        .font(.subheadline)
                    Picker("Ajout d'une consent string valide", selection: $consentStringEnabled) {
                        Text("Oui").tag(true)
                        Text("Non").tag(false)
                    }
                    .pickerStyle(.menu)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Type de player")
                        .font(.subheadline)

                    Picker("Newplayer", selection: newplayerModeBinding) {
                        ForEach(NewplayerMode.allCases) { mode in
                            Text(mode.label).tag(mode)
                        }
                    }
                    .pickerStyle(.menu)

                    if newplayerMode == .recette {
                        TextField("Nom de la branche — ex: dev ou SUP-123", text: $newplayerBranchName)
                            .font(.footnote.monospaced())
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                            .keyboardType(.numbersAndPunctuation)
                            .foregroundStyle(newplayerBranchName.isEmpty ? .secondary : .primary)
                    }

                    if newplayerMode == .local {
                        TextField("Adresse IP — ex: 192.168.X.X:YYYY", text: $newplayerLocalIP)
                            .font(.footnote.monospaced())
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)
                            .keyboardType(.numbersAndPunctuation)
                            .foregroundStyle(newplayerLocalIP.isEmpty ? .secondary : .primary)
                    }
                }
            } header: {
                Text("Configuration")
            }

            // ── Override manuel de l'URL du player ──────────────────────────
            Section {
                VStack(alignment: .leading, spacing: 4) {
                    Text("URL du player override (iOS 18+)")
                        .font(.subheadline)
                    TextField(
                        "Remplacera les paramètres précédents",
                        text: $playerURLOverride
                    )
                    .font(.footnote.monospaced())
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .keyboardType(.URL)
                    .lineLimit(nil)
                    .fixedSize(horizontal: false, vertical: true)
                    .foregroundStyle(playerURLOverride.isEmpty ? .secondary : .primary)
                }
            }

            // ── Résumé de la configuration active ───────────────────────────
            Section {
                VStack(alignment: .leading, spacing: 6) {
                    HStack {
                        Text("URL iframe")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        Spacer()
                        Button {
                            UIPasteboard.general.string = iframeURLPreview
                            withAnimation { copiedURL = true }
                            Task {
                                try? await Task.sleep(nanoseconds: 1_200_000_000)
                                withAnimation { copiedURL = false }
                            }
                        } label: {
                            Label(copiedURL ? "Copiée" : "Copier l'url", systemImage: copiedURL ? "checkmark" : "doc.on.doc")
                                .font(.caption)
                        }
                        .buttonStyle(.bordered)
                        .controlSize(.mini)
                    }
                    Text(iframeURLPreview)
                        .font(.caption2.monospaced())
                        .foregroundStyle(.secondary)
                        .lineLimit(nil)
                        .fixedSize(horizontal: false, vertical: true)
                        .textSelection(.enabled)
                }
                .padding(.vertical, 2)
            } header: {
                Text("URL du player générée")
            } footer: {
                Text("L'URL ci-dessus est construite en temps réel.")
            }
        }
        .navigationTitle("Config Instream")
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private func paramRow(label: String, value: String, placeholder: String) -> some View {
        LabeledRow(label) {
            Text(value.isEmpty ? placeholder : value)
                .foregroundStyle(value.isEmpty ? .tertiary : .primary)
                .font(.footnote.monospaced())
                .lineLimit(1)
                .textSelection(.enabled)
        }
    }
}
