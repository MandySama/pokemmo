# TWL source integration

Upstream: https://github.com/MatthiasMann/twl (master archive, fetched 2026-10-12).
Archive SHA-256: 816e605e2a12e20723433c7a66b2525a5ce33a09877737cbeabcaff5afc1680a.

The upstream Java sources retain their original BSD copyright/license headers.
The core sources live under `twl-core/src/main/java/de/matthiasmann/twl`, using Maven's
standard source root so IDEs resolve packages correctly. The original LWJGL2 renderer and input packages are not included in this module;
this project uses LWJGL3/GLFW.

The root POM builds `twl-core` and `client` as a Maven reactor. The client declares
`top.totodile:twl-core:1.0-SNAPSHOT` as a normal compile dependency; xpp3 is transitive.
This is a locally built module, not an upstream release downloaded from Maven Central.
Run `mvn compile` from the project root. In IDEA reload the root Maven project and
run `top.totodile.pokemmo.Main` using the `client` module classpath.

`GlfwTwlRenderer` implements TWL's Renderer/Texture/Image contracts for the current
login screen, including theme-owned texture cleanup, tinting, clipping and tiled
atlas regions. The login form follows the reference login-window/base-frame and
DialogLayout themes (two columns, centered window, remember/register/login row).
NotoSans glyphs are rasterized with Java2D and cached as GPU textures. GLFW callbacks
forward mouse, text and translated keyboard events to TWL. Custom cursors,
rotated images and gradients are not yet implemented.
Unsupported required operations fail explicitly. No substitute Widget/GUI/ThemeManager
classes are used.

The reference client's customized TWL depends on game-global objects. This integration
uses upstream TWL core instead and ports only the minimal login form, scrolling background and logo theme/layout.
The complete reference theme includes custom extensions (e.g. fontGen) and cannot yet
be loaded unchanged by upstream TWL.
