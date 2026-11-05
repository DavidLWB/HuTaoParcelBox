import com.davlike.hutaoparcelbox.UrlPolicy;

public final class JdUrlPolicyTest {
    private static int checked;
    private static void expect(boolean expected, String url) {
        checked++;
        if (UrlPolicy.isJdAllowed(url) != expected)
            throw new AssertionError("Unexpected JD allowlist result: " + url);
    }
    private static void expectSearch(boolean expected, String url) {
        checked++;
        if (UrlPolicy.isJdSearch(url) != expected)
            throw new AssertionError("Unexpected JD search result: " + url);
    }
    private static void expectLogin(boolean expected, String url) {
        checked++;
        if (UrlPolicy.isJdLoginPage(url) != expected)
            throw new AssertionError("Unexpected JD login result: " + url);
    }
    private static void expectMy(boolean expected, String url) {
        checked++;
        if (UrlPolicy.isJdMyPage(url) != expected)
            throw new AssertionError("Unexpected JD my-page result: " + url);
    }
    public static void main(String[] args) {
        for (String url : new String[]{UrlPolicy.JD_PARCELS_ENTRY, UrlPolicy.JD_LOGIN_ENTRY,
                "https://logistics-mrd.jd.com/", "https://passport.jd.com/new/login.aspx",
                "https://plogin.m.jd.com/login/login", "https://www.jd.com/", "https://3.cn/abc"})
            expect(true, url);
        for (String url : new String[]{null, "", "http://logistics-mrd.jd.com/v3/search",
                "https://logistics-mrd.jd.com.evil.example/v3/search",
                "https://eviljd.com/", "https://jd.com@evil.example/",
                "https://jd.com:444/v3/search", "javascript:alert(1)"})
            expect(false, url);
        expectSearch(true, UrlPolicy.JD_PARCELS_ENTRY);
        expectSearch(false, "https://logistics-mrd.jd.com/v3/other");
        expectSearch(false, "https://passport.jd.com/v3/search");
        expectLogin(true, "https://passport.jd.com/new/login.aspx");
        expectLogin(true, "https://plogin.m.jd.com/login/login");
        expectLogin(false, UrlPolicy.JD_PARCELS_ENTRY);
        expectLogin(false, "https://www.jd.com/");
        expectMy(true, UrlPolicy.JD_LOGIN_ENTRY);
        expectMy(false, UrlPolicy.JD_PARCELS_ENTRY);
        expectMy(false, "https://logistics-mrd.jd.com/v3/my/extra");
        System.out.println("PASS: " + checked + " JD navigation allowlist cases.");
    }
}
