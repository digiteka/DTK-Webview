import Foundation

struct HTMLGenerator {

    // MARK: - URL builder

    /// Construit l'URL de l'iframe cross-origin (format chemin ultimedia)
    static func iframeURL(
        mdtk: String,
        zone: String,
        src: String,
        autoplay: Int,
        sound: Int,
        ad: Int,
        newplayer: String? = nil,
        refererURL: String,
        tagParam: String? = nil,
        consentString: String
    ) -> String {
        var url = "https://www.ultimedia.com/deliver/generic/iframe"
        url += "/mdtk/\(mdtk)"
        url += "/zone/\(zone)"
        url += "/src/\(src)"
        url += "/showtitle/1"
        url += "/autoplay/\(autoplay)"
        url += "/sound/\(sound)"
        url += "/ad/\(ad)"

        var queryItems: [String] = []
        let encodedReferer = refererURL
            .addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if !encodedReferer.isEmpty {
            queryItems.append("urlfacebook=\(encodedReferer)")
        }
        if !consentString.isEmpty {
            queryItems.append("gdprconsentstring=\(consentString)")
        }
        if let newplayerDomain = newplayer, !newplayerDomain.isEmpty {
            queryItems.append("newplayer=\(newplayerDomain)")
        }
        if let tag = tagParam, !tag.isEmpty {
            let encodedTag = tag.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? tag
            queryItems.append("tagparam=\(encodedTag)")
        }
        if !queryItems.isEmpty {
            url += "?" + queryItems.joined(separator: "&")
        }
        return url
    }

    // MARK: - HTML Instream

    /// Génère une page simulant un article de presse avec le player intégré en instream
    static func generateInstream(
        mdtk: String,
        zone: String,
        src: String,
        autoplay: Int,
        sound: Int,
        ad: Int,
        refererURL: String,
        consentString: String,
        newplayer: String? = nil,
        tagParam: String? = nil
    ) -> String {
        let playerSrc = iframeURL(
            mdtk: mdtk,
            zone: zone,
            src: src,
            autoplay: autoplay,
            sound: sound,
            ad: ad,
            newplayer: newplayer,
            refererURL: refererURL,
            tagParam: tagParam,
            consentString: consentString
        )

        return """
        <!doctype html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1" id="mvp">
            <link rel="canonical" href="\(refererURL)">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }

                body {
                    background: #fff;
                    color: #1a1a1a;
                    font-family: Georgia, 'Times New Roman', serif;
                    font-size: 17px;
                    line-height: 1.7;
                }

                .article {
                    max-width: 680px;
                    margin: 0 auto;
                    padding: 24px 16px 48px;
                }

                .article-category {
                    font-family: -apple-system, sans-serif;
                    font-size: 11px;
                    font-weight: 700;
                    letter-spacing: 0.12em;
                    text-transform: uppercase;
                    color: #c00;
                    margin-bottom: 12px;
                }

                .article-title {
                    font-size: 26px;
                    font-weight: 700;
                    line-height: 1.25;
                    margin-bottom: 12px;
                    color: #111;
                }

                .article-meta {
                    font-family: -apple-system, sans-serif;
                    font-size: 12px;
                    color: #888;
                    margin-bottom: 24px;
                    padding-bottom: 16px;
                    border-bottom: 1px solid #e8e8e8;
                }

                p { margin-bottom: 18px; }

                .player-wrapper {
                    position: relative;
                    padding-bottom: 56.25%;
                    height: 0;
                    width: 100%;
                    margin: 24px 0;
                }

                .player-wrapper iframe {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    border: 0;
                }
            </style>
        </head>
        <body>
            <article class="article">

                <!-- Contenu avant le player -->
                <p class="article-category">Économie</p>
                <h1 class="article-title">Lorem ipsum dolor sit amet, consectetur adipiscing elit</h1>
                <p class="article-meta">Par Jean Dupont · 30 juin 2026 · 4 min de lecture</p>

                <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</p>

                <p>Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium.</p>

                <p>Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet.</p>

                <!-- Player instream -->
                <div class="player-wrapper">
                    <iframe
                        id="um_ultimedia_wrapper_iframeUltimedia"
                        scrolling="no"
                        marginwidth="0"
                        marginheight="0"
                        hspace="0"
                        vspace="0"
                        allowfullscreen="true"
                        allow="autoplay"
                        src="\(playerSrc)">
                    </iframe>
                </div>

                <!-- Contenu après le player -->
                <p>Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur. Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur.</p>

                <p>At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident. Similique sunt in culpa qui officia deserunt mollitia animi.</p>

                <p>Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe.</p>

            </article>

            <script>
                // Relayer les postMessage cross-origin du player vers Swift
                window.addEventListener('message', function(e) {
                    if (!window.webkit || !window.webkit.messageHandlers || !window.webkit.messageHandlers.playerEvents) return;
                    var msg = typeof e.data === 'string' ? e.data : JSON.stringify(e.data);
                    if (!msg) return;
                    window.webkit.messageHandlers.playerEvents.postMessage({ event: msg, origin: e.origin });
                });
            </script>
        </body>
        </html>
        """
    }
}
