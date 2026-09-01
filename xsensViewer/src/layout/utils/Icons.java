package layout.utils;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/**
 * Small vector icons drawn as SVG paths.
 *
 * <p>JMetro shipped {@code MDL2IconFont}, but that depends on the Segoe MDL2
 * Assets font, which exists on Windows and not on macOS or Linux - the icons
 * came out as empty boxes on two of the three platforms this runs on. Paths
 * render identically everywhere and take their colour from CSS, so they follow
 * the light/dark theme.
 *
 * <p>Mirrors the same class in the SoundNet Firmware Updater, so an icon that
 * appears in both applications is the same drawing.
 *
 * @author Jamie Macaulay
 */
public final class Icons {

	/** The paths below are drawn on a 24x24 grid. */
	private static final double SOURCE_SIZE = 24.0;

	private static final double DEFAULT_SIZE = 15.0;

	private static final String REFRESH =
			"M17.65 6.35A7.958 7.958 0 0 0 12 4c-4.42 0-7.99 3.58-8 8s3.58 8 8 8c3.73 0 "
					+ "6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 "
					+ "6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z";

	private static final String ADD = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z";

	private static final String TABS =
			"M21 3H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 "
					+ "16H3V5h10v4h8v10z";

	private static final String TILES = "M3 3h8v8H3z M13 3h8v8h-8z M3 13h8v8H3z M13 13h8v8h-8z";

	private static final String SUN =
			"M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 1-.45 "
					+ "1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 1-1s-.45-1-1-1h-2c-.55 "
					+ "0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 "
					+ "18v2c0 .55.45 1 1 1s1-.45 1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 0 "
					+ "0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 "
					+ "4.58zm12.37 12.37a.996.996 0 0 0-1.41 0 .996.996 0 0 0 0 1.41l1.06 1.06c.39.39 1.03.39 "
					+ "1.41 0a.996.996 0 0 0 0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 0 0 0-1.41.996.996 0 0 "
					+ "0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36a.996.996 "
					+ "0 0 0 0-1.41.996.996 0 0 0-1.41 0l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 "
					+ "0l1.06-1.06z";

	private static final String MOON =
			"M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 0 1-4.4 2.26 5.403 5.403 0 0 "
					+ "1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z";

	private Icons() {
	}

	/** Circular arrow - ask the sensor for fresh values. */
	public static Node refresh() {
		return icon(REFRESH);
	}

	/** Plus - add another sensor tab. */
	public static Node add() {
		return icon(ADD);
	}

	/** A tabbed window - switch to the tabbed layout. */
	public static Node tabs() {
		return icon(TABS);
	}

	/** Four squares - switch to the tiled layout. */
	public static Node tiles() {
		return icon(TILES);
	}

	/** Sun - switch to the light theme. */
	public static Node sun() {
		return icon(SUN);
	}

	/** Crescent moon - switch to the dark theme. */
	public static Node moon() {
		return icon(MOON);
	}

	private static Node icon(String path) {
		return icon(path, DEFAULT_SIZE);
	}

	private static Node icon(String path, double size) {
		SVGPath shape = new SVGPath();
		shape.setContent(path);
		// Fill comes from the ".icon" rule in style.css so it flips with the theme.
		shape.getStyleClass().add("icon");
		double factor = size / SOURCE_SIZE;
		shape.getTransforms().add(new Scale(factor, factor));
		// A Group reports the transformed size, so the button lays out correctly.
		return new Group(shape);
	}
}
