import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author IzumiiKonata
 * Date: 2025/10/27 21:28
 */
public class A {
    public static void main(String[] args) {
//        for (int i = 0; i < 256; i++) {
//            System.out.println((char) i);
//        }
        char[] charArray = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".toCharArray();

        List<Character> list = new ArrayList<>();
        for (char c : charArray) {
            list.add(c);
        }

        list = list.stream().distinct().sorted(Character::compareTo).collect(Collectors.toList());

        for (char c : list) {
            System.out.print(c);
        }

        System.out.println();

        int start = list.get(0);

        for (int i = 0; i < list.size(); i++) {
            char c = list.get(i);
            char prev = i == 0 ? '\u0000' : list.get(i - 1);
            if (c != prev + 1) {
                System.out.println(start + " - " + ((int) prev));
                start = c;
            }
        }
    }
}
