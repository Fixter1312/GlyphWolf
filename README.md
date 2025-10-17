# 🐺 GlyphWolf — Nothing Glyph Matrix Wolf Demo

Minimalna aplikacja Android w Kotlinie:
- Wyświetla wilka na ekranie z macierzy 25×25.
- Przyciemnia obraz w zależności od poziomu baterii.
- Wysyła obraz przez SDK Glyph Matrix (`.aar`).
- Buduje się przez GitHub Actions jako `.apk`.

## 📦 Build
```bash
./gradlew :app:assembleDebug
```

Plik APK pojawi się w:
```
app/build/outputs/apk/debug/
```
