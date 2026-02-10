import tritium.ncm.OptionsUtil;
import tritium.ncm.RequestUtil;
import tritium.ncm.api.CloudMusicApi;
import tritium.screens.ncm.LyricLine;
import tritium.screens.ncm.LyricParser;

import java.util.List;
import java.util.Scanner;

/**
 * @author IzumiiKonata
 * Date: 2026/2/10 20:41
 */
public class NCMLyricsObtainer {

    public static void main(String[] args) {
        RequestUtil.RequestAnswer registerAnonimous = CloudMusicApi.registerAnonimous();

        System.out.println(registerAnonimous);
        String[] cookies = registerAnonimous.getCookies();

        if (cookies == null)
            return;

        OptionsUtil.setCookie(String.join(" ",  cookies));

        Scanner sn = new Scanner(System.in).useDelimiter("\n");

        System.out.print("Music ID>> ");

        while (sn.hasNext()) {
            String id = sn.next();

            long l = 0;
            try {
                l = Long.parseLong(id);
            } catch (NumberFormatException e) {
                System.err.println("Not valid number: " + l);
                continue;
            }

            RequestUtil.RequestAnswer lyricResp = CloudMusicApi.lyricNew(l);
            System.out.println(lyricResp);
//            List<LyricLine> parsed = LyricParser.parse(lyricResp.toJsonObject());
//
//            for (LyricLine line : parsed) {
//                System.out.println(line.lyric);
//            }

            System.out.print("\nMusic ID>> ");
        }

    }

}
