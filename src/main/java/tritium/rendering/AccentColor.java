package tritium.rendering;

import lombok.Getter;

import java.awt.*;

@Getter
public class AccentColor {

	private final String name;
	
	private final Color color1;
    private final Color color2;

	public AccentColor(String name, Color color1, Color color2) {
		this.name = name;
		this.color1 = color1;
		this.color2 = color2;
	}

}
