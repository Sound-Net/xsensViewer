#!/usr/bin/env python3
"""Regenerates the application icons from the master artwork.

Run from the repository root:  python3 tools/make-icons.py
Needs Pillow (pip install pillow). The .icns step additionally needs macOS,
and is skipped elsewhere - the committed .icns only changes when the artwork
does, so a non-mac contributor can leave it alone.

The master art is a rounded square drawn on a flat page, and that page has to
become transparent. Two obvious ways to drop it do not work:

  * keying out the page colour punches holes in every part of the artwork that
    happens to share it - the dolphin belly, the net and the axis labels on the
    old white-page master, the near-black facets on the current dark one;
  * flooding inwards from the border leaks through those same features wherever
    they run right up to the edge of the artwork.

So the silhouette is traced instead. Each row and column of the master is
reduced to the span between its first and last artwork pixel, and the two sets
of spans are intersected: that fills interior features whatever their colour,
and follows the real outline rather than fitting a shape to it - the corners
here are a squircle, which a rounded rectangle does not match. The spans are
inset a few pixels because the master is a JPEG whose edge is an anti-aliased
ramp from the page colour into the artwork - cutting exactly on the outline
would leave a fringe of the page behind.

The page colour is measured from the master corners rather than assumed, so
both the original white-page artwork and the current black-page one work.
"""
import os
import subprocess
import sys

try:
    from PIL import Image, ImageChops, ImageDraw, ImageFilter
except ImportError:
    sys.exit("Pillow is required: pip install pillow")

# Where the master artwork lives, and where the generated icons go.
SOURCE = "src/resources/xsensviewer_icon.jpg"
# Installer icons. Kept out of src/, because the whole of src/ is a resource
# root here and everything under it is copied into the jar.
PACKAGING_DIR = "packaging"
# The window/taskbar icon, which has to be on the classpath.
APP_ICON = "src/resources/app-icon.png"

# A pixel counts as artwork when some channel differs from the page colour by
# more than this.
PAGE_TOL = 20
# Consecutive artwork pixels required before an edge is believed. The master is
# a JPEG, so specks of ringing noise sit out on the page a long way from the
# artwork; without this they drag the spans out to meet them.
EDGE_RUN = 6
# Pixels trimmed off the silhouette to clear the JPEG edge ramp. Measured on
# the master art: the fringe is gone by 6, out of an ~1170px icon.
INSET = 6
# Blur applied to the traced silhouette, so its edge is not left aliased by the
# one-row-at-a-time tracing.
FEATHER = 1.2
# Sizes packed into the .ico, which is what Windows Explorer and the taskbar read.
ICO_SIZES = (16, 24, 32, 48, 64, 128, 256)
# The window icon is scaled by the toolkit, so one reasonably sized image does.
APP_ICON_PX = 256


def page_colour(im):
    """The page colour, taken from the four corners of the master.

    The median of the four, so one stray corner cannot decide it alone.
    """
    px = im.load()
    w, h = im.size
    corners = [px[0, 0], px[w - 1, 0], px[0, h - 1], px[w - 1, h - 1]]
    return tuple(sorted(c[i] for c in corners)[1] for i in range(3))


def artwork_mask(im, page):
    """Binary image: white where the master differs from the page colour."""
    diff = None
    for channel, level in zip(im.split(), page):
        d = channel.point(lambda v, level=level: abs(v - level))
        diff = d if diff is None else ImageChops.lighter(diff, d)
    return diff.point(lambda v: 255 if v > PAGE_TOL else 0)


def span(length, art):
    """First and last index along one line that are really artwork.

    art(i) reads the binary mask at step i. An edge only counts once EDGE_RUN
    pixels in a row are artwork, which is what rejects the JPEG specks.
    """
    ends = []
    for indices in (range(length), range(length - 1, -1, -1)):
        run = 0
        for i in indices:
            if art(i):
                run += 1
                if run >= EDGE_RUN:
                    ends.append(i)
                    break
            else:
                run = 0
        else:
            return None
    # Each end was found EDGE_RUN-1 pixels inside its own run, so push the two
    # of them back out to the ends of the artwork.
    return min(ends) - EDGE_RUN + 1, max(ends) + EDGE_RUN - 1


def silhouette(im, page):
    """Alpha channel for the artwork: page dropped, everything else kept."""
    w, h = im.size
    mask = artwork_mask(im, page).load()

    # Rows and columns are traced separately and then intersected. Insetting
    # rows alone would leave the corners proud, as nothing there gets trimmed
    # vertically.
    by_row = Image.new("L", (w, h), 0)
    draw = ImageDraw.Draw(by_row)
    for y in range(h):
        s = span(w, lambda x, y=y: mask[x, y])
        if s and s[1] - s[0] > 2 * INSET:
            draw.line([(s[0] + INSET, y), (s[1] - INSET, y)], fill=255)

    by_col = Image.new("L", (w, h), 0)
    draw = ImageDraw.Draw(by_col)
    for x in range(w):
        s = span(h, lambda y, x=x: mask[x, y])
        if s and s[1] - s[0] > 2 * INSET:
            draw.line([(x, s[0] + INSET), (x, s[1] - INSET)], fill=255)

    return ImageChops.darker(by_row, by_col)


def master_image(src):
    """The artwork, cut out of its page and centred on a square canvas."""
    im = Image.open(src).convert("RGB")
    page = page_colour(im)
    alpha = silhouette(im, page)
    box = alpha.getbbox()
    if box is None:
        sys.exit(f"{src}: no artwork found against a page of {page}")
    print(f"  {os.path.basename(src)}: page {page}, artwork {box}")

    im.putalpha(alpha.filter(ImageFilter.GaussianBlur(FEATHER)))
    art = im.crop(box)

    # Square canvas, so nothing is distorted when the icon is scaled down.
    side = max(art.size)
    square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    square.paste(art, ((side - art.width) // 2, (side - art.height) // 2))
    return square.resize((1024, 1024), Image.LANCZOS)


def write_icns(master, path):
    """iconutil is macOS-only and wants a folder of specifically-named sizes."""
    if sys.platform != "darwin":
        print(f"  skipped {path} (needs macOS iconutil)")
        return
    iconset = path.replace(".icns", ".iconset")
    os.makedirs(iconset, exist_ok=True)
    try:
        for size in (16, 32, 128, 256, 512):
            master.resize((size, size), Image.LANCZOS).save(
                os.path.join(iconset, f"icon_{size}x{size}.png"))
            master.resize((size * 2, size * 2), Image.LANCZOS).save(
                os.path.join(iconset, f"icon_{size}x{size}@2x.png"))
        subprocess.run(["iconutil", "-c", "icns", iconset, "-o", path], check=True)
        print(f"  wrote {path}")
    finally:
        subprocess.run(["rm", "-rf", iconset], check=False)


def build(src, packaging_dir, app_icon_path):
    master = master_image(src)
    os.makedirs(packaging_dir, exist_ok=True)

    png = os.path.join(packaging_dir, "icon.png")
    ico = os.path.join(packaging_dir, "icon.ico")
    icns = os.path.join(packaging_dir, "icon.icns")

    master.save(png)
    print(f"  wrote {png}")
    # bitmap_format="bmp" rather than Pillow's default PNG-compressed entries:
    # NSIS's Icon directive and Windows' own resource stamping both accept BMP
    # entries everywhere, while PNG-compressed .ico files are only handled in
    # some places and fail in others. The file is bigger; it is still ~360KB.
    master.save(ico, sizes=[(s, s) for s in ICO_SIZES], bitmap_format="bmp")
    print(f"  wrote {ico}")
    write_icns(master, icns)

    os.makedirs(os.path.dirname(app_icon_path), exist_ok=True)
    master.resize((APP_ICON_PX, APP_ICON_PX), Image.LANCZOS).save(app_icon_path)
    print(f"  wrote {app_icon_path}")


if __name__ == "__main__":
    build(SOURCE, PACKAGING_DIR, APP_ICON)
