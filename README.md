DOGAデータをgl.enchant.js用にコンバートするWebサービス
------------------------------------------------------

play2.0.4で作ってます。

とりあえず動かしてみる方法
--------------------------

1. developブランチをチェックアウト
    <pre>
    git clone https://github.com/daishihmr/dogencha.git
    cd dogencha
    git checkout -b develop origin/develop
    </pre>

2. DOGA-L3をセットアップ
 1. COMMONディレクトリ・DATAディレクトリ以下のディレクトリ・ファイル名をすべて小文字にする

3. TwitterからConsumerKeyを取得
 1. dogenchaディレクトリにファイルtwitter.propertiesを作成し、以下のように記述
    <pre>
    twitter4j.oauth.consumerKey=(ConsumerKey)
    twitter4j.oauth.consumerSecret=(ConsumerSecret)
    </pre>

4. conf/application.confを編集
 1. データベース
    <pre>
    db.default.driver=org.h2.Driver
    #db.default.url="jdbc:h2:tcp:localhost:9090/database/default"
    db.default.url="jdbc:h2:file:database/devel"
    </pre>

 2. DOGAインストールディレクトリ
    <pre>
    dev7.doga.parser.dogaCommonDir = /Users/daishi_hmr/tool/dogacga/common
    dev7.doga.parser.dogaDataDir = /Users/daishi_hmr/tool/dogacga/data
    </pre>
