# DOGAデータをgl.enchant.js用にコンバートするWebサービス

play2.0.4で作ってます。

## とりあえず動かしてみる方法

### 1. Play Framework 2.0.4をインストール

### 2. developブランチをチェックアウト
<pre>
    git clone https://github.com/daishihmr/dogencha.git
    cd dogencha
    git checkout -b develop origin/develop
</pre>

### 3. DOGA-L3をセットアップ
#### COMMONディレクトリ・DATAディレクトリ以下のディレクトリ・ファイル名をすべて小文字にする

![小文字化](http://cloud.github.com/downloads/daishihmr/dogencha/tolowercase.png)

### 4. TwitterからConsumerKeyを取得
dogenchaディレクトリにファイルtwitter.propertiesを作成し、以下のように記述
<pre>
    twitter4j.oauth.consumerKey=(ConsumerKey)
    twitter4j.oauth.consumerSecret=(ConsumerSecret)
</pre>

### 5. conf/application.confを編集
#### データベース
<pre>
    db.default.driver=org.h2.Driver
    #db.default.url="jdbc:h2:tcp:localhost:9090/database/default"
    db.default.url="jdbc:h2:file:database/devel"
</pre>
#### DOGACGAのcommonディレクトリとdataディレクトリの位置を設定
<pre>
    dev7.doga.parser.dogaCommonDir = C:/Program Files/DOGACGA/common
    dev7.doga.parser.dogaDataDir = C:/Program Files/DOGACGA/data
</pre>

### 6. 起動
以下のコマンドを実行
<pre>
    play run
</pre>

#### ブラウザで表示する
<http://localhost:9000/>
