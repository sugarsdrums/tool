package sugars.drums;

public class Const {

    public static final String HTML_FILE_NAME = "schedule20%s.html";

    public static final String MOBILE_FILE_NAME = "info%s.html";

    public static final String LATTE = "Latte";

    public static final String LATTE_LINK = """
            <a href="http://www.lattecafe.jp/" target="_blank">Latte</a>""";

    public static final String FROM = "０１２３４５６７８９ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ（）’－／\\<>";

    public static final String TO = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ()'-/￥＜＞";

    public static final String TEMPLATE = """
                        <div class="schedule-item">
                          <div class="live-date">%s</div>
                          <div class="live-band">%s</div>
                          <div class="live-place">%s</div>
                        </div>
                        <div class="schedule-detail">
                          <div class="live-title">%s</div>
                          <div class="live-info">
                            <div class="live-time">%s</div>
                            <div class="live-price">%s</div>
                          </div>
                          <div class="live-member">
                            %s
                          </div>
                          <div class="live-address">%s</div>
                        </div>
                    """;
}
