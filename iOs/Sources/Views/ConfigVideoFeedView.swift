import SwiftUI

struct ConfigVideoFeedView: View {
    static let defaultConsentString = "CQlo8kAQlo8kAAHABBENCiFsAP_gAEPgAAAALAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABBYAgPYX7iKQsTiQI_BdkASGL2CkAVBgEIIBBAASAMQBABJGAWswgRAEQAAQBAAIABASIAAAEIAAIAIAAABAAJCIQgAgCACIAABQIAAACKEAAAAEIASAAAwACEBhKgAAgEAAggANUCAsAACAEEUAABAAAoBAQgAAAAAAECAAABAAAAEyAAkAADgBAAgAAAAIhgAAAFAAAQAAAAgACAAAACAAAAAAAAAAEAAABEACAAAAAAIAwSADAAEGSB0AGAAIMkEIAMAAQZIJQAYAAgyQUgAwABBkgtABgACDJAAA.ILAEB7C_cRSFicSZn4LsgSQxewUhCoMAhBAIIACwBiAIAJJwG1mECIAjAgCAKABIAICRAAQAlCADABAAAAIABITCEIEAQARAAIqBAAAARQgIACAhAGQAAGAAQgMJUAgEAkAMECBqoQFhAAQAgigAQIAAlAICFAAAAAAAgQAAAIAAAAmwQEgAAcAIAEAAAAFEMAAAAoAECAAAAEAAQAAAAQBAAAAAAAAAgAQAIgAQAAAAABAA.f_wAH_wAAAAA"

    @AppStorage("videoFeedMdtk") private var mdtk = "01573101"
    @AppStorage("videoFeedZoneId") private var zoneId = 0
    @AppStorage("videoFeedAdunitPath") private var adunitPath = ""
    @AppStorage("videoFeedVideoId") private var videoId = ""
    @AppStorage("videoFeedCarrouselHeight") private var carrouselHeight = 280
    @AppStorage("videoFeedVfBranch") private var vfBranch = ""
    @AppStorage("videoFeedCarrBranch") private var carrBranch = ""
    @AppStorage("videoFeedConsentStringEnabled") private var consentStringEnabled = true

    private var consentString: String? {
        consentStringEnabled ? Self.defaultConsentString : nil
    }

    private var zoneIdText: Binding<String> {
        Binding(
            get: { zoneId == 0 ? "" : String(zoneId) },
            set: { zoneId = Int($0) ?? 0 }
        )
    }

    private var carrouselHeightText: Binding<String> {
        Binding(
            get: { String(carrouselHeight) },
            set: { carrouselHeight = Int($0) ?? 0 }
        )
    }

    var body: some View {
        Form {
            Section {
                LabeledRow("MDTK") {
                    TextField("ex: 01xxxxxx", text: $mdtk)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }

                LabeledRow("Zone ID") {
                    TextField("ex: 3", text: zoneIdText)
                        .multilineTextAlignment(.trailing)
                        .keyboardType(.numberPad)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("Adunit Path")
                        .font(.subheadline)
                    TextField("ex: /1234/adunit/path", text: $adunitPath)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .foregroundStyle(adunitPath.isEmpty ? .secondary : .primary)
                }

                LabeledRow("Video ID") {
                    TextField("ex: 3v83mr3", text: $videoId)
                        .multilineTextAlignment(.trailing)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                }

                LabeledRow("Hauteur du carrousel (px)") {
                    TextField("", text: carrouselHeightText)
                        .multilineTextAlignment(.trailing)
                        .keyboardType(.numberPad)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Picker("Consent String", selection: $consentStringEnabled) {
                        Text("Oui").tag(true)
                        Text("Non").tag(false)
                    }
                    .pickerStyle(.menu)
                }
            } header: {
                Text("Configuration")
            }

            Section {
                VStack(alignment: .leading, spacing: 4) {
                    Text("VF_BRANCH")
                        .font(.subheadline)
                    TextField("ex: local, SUP-123...", text: $vfBranch)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .keyboardType(.numbersAndPunctuation)
                        .foregroundStyle(vfBranch.isEmpty ? .secondary : .primary)
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text("CARR_BRANCH")
                        .font(.subheadline)
                    TextField("ex: local, SUP-123...", text: $carrBranch)
                        .font(.footnote.monospaced())
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .keyboardType(.numbersAndPunctuation)
                        .foregroundStyle(carrBranch.isEmpty ? .secondary : .primary)
                }
            } header: {
                Text("Branches Amplify")
            }
        }
        .navigationTitle("Configuration VideoFeed")
        .navigationBarTitleDisplayMode(.inline)
    }
}
