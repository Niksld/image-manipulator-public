package com.imgmanip;

/*Tato třída slouží pouze pro kompilaci pomocí Maven.
Vzhledem k tomu, že používáme JavaFX, ale chceme spouštět JAR soubor i bez nainstalovaného JDK a JavaFX.
Pomocí pluginu maven-shade můžeme tyto balíčky vložit přímo do JAR souboru a vytvořit tak UberJar.
Bohužel shade a JavaFX spolu nehrají úplně dobře a není úplně doporučováno je používat dohromady.
Vzhledem k našemu použití to však nevadí, jelikož tohle nebude kód, který bude deploynutý někde na
opravdovém serveru či jako desktopová aplikace. Tím pádem nám stačí obejít chybu v kompilaci tímto
způsobem - takto se zkompiluje projekt v pořádku i s dodatečnými libraries

*/

public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
