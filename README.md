# Player Board (Fabric, Minecraft 26.3)

Eine 3x3-Block grosse Wandtafel, die alle Online-Spieler (wie die Tab-Liste) anzeigt.

## Bauen
Voraussetzung: JDK 25.
1. `gradle wrapper --gradle-version 9.7.1`   (einmalig, erzeugt gradlew)
2. `./gradlew build`  -> `build/libs/playerboard-1.0.0.jar`
3. Test im Spiel: `./gradlew runClient`

## Installieren
`playerboard-1.0.0.jar` + Fabric API (0.161.0+26.3) in `mods/` (Fabric Loader >= 0.19.5).
Auf einem Server muss die Mod auf Server UND Clients installiert sein (neuer Block).

## Benutzen
Item "Spielertafel" (Kreativ-Tab "Spielertafel" oder Rezept: 8x Schwarzer Beton + Kompass).
Klicke auf eine Wand: der Block wird als Mitte gesetzt, die Tafel belegt 3x3 Bloecke davor.
Abbauen eines Teils entfernt die ganze Tafel.
