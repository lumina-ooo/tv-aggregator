# Agrégateur TV - Plateformes de Streaming Francophones

![Android TV](https://img.shields.io/badge/Android%20TV-API%2021+-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-TV-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

Une application Android TV gratuite qui regroupe toutes les plateformes de streaming francophones gratuites dans une interface unifiée optimisée pour la télécommande.

## 📺 Plateformes Supportées

### 🇫🇷 France
- **TF1+** - Programmes et replays de TF1
- **France.tv** - Service public français
- **M6+/6play** - Programmes du groupe M6
- **Molotov** - TV française en direct et replay

### 🇧🇪 Belgique
- **RTL TVI+** - Télévision RTL belge
- **Auvio/RTBF** - Service public belge francophone

### 🌍 Franco-Allemande & International
- **Arte** - Culture et documentaires
- **Pluto TV** - Plus de 100 chaînes gratuites
- **Rakuten TV** - Films et séries avec publicité

## ✨ Fonctionnalités

- 🎯 **Catalogue unifié** de toutes les plateformes francophones
- 🔍 **Recherche intelligente** par titre, genre ou catégorie
- 🎮 **Navigation D-pad** optimisée pour télécommande TV
- 📱 **Lancement automatique** des applications installées
- 🏪 **Redirection Play Store** pour les apps non installées
- 🎨 **Interface sombre** optimisée pour grands écrans 4K
- 📊 **Détection d'installation** en temps réel

## 🚀 Installation

### Option 1: Depuis les Releases GitHub

1. Rendez-vous dans [Releases](https://github.com/lumina-ooo/tv-aggregator/releases)
2. Téléchargez le dernier fichier `.apk`
3. Transférez-le sur votre Android TV via USB ou réseau
4. Installez l'APK (voir instructions détaillées ci-dessous)

### Option 2: Installation par Sideloading (Sony Bravia)

#### Prérequis
- Android TV sous Android 9.0+ (API 28+)
- Accès développeur activé
- Connexion internet

#### Étapes détaillées pour Sony Bravia

1. **Activer le mode développeur**
   - Aller dans `Paramètres` → `À propos de l'appareil`
   - Appuyer 7 fois sur `Numéro de build` jusqu'à voir "Mode développeur activé"

2. **Activer les sources inconnues**
   - Aller dans `Paramètres` → `Sécurité et restrictions`
   - Activer `Sources inconnues` pour l'installation d'apps

3. **Télécharger l'APK**
   ```bash
   # Via ordinateur connecté au même réseau
   wget https://github.com/lumina-ooo/tv-aggregator/releases/latest/download/tv-aggregator.apk
   ```

4. **Transférer vers la TV**
   - Méthode USB: Copier l'APK sur clé USB, insérer dans la TV
   - Méthode réseau: Utiliser un gestionnaire de fichiers comme X-plore

5. **Installer l'APK**
   - Ouvrir le gestionnaire de fichiers sur la TV
   - Naviguer vers l'APK et sélectionner
   - Confirmer l'installation

6. **Lancer l'application**
   - L'app apparaîtra dans le lanceur Android TV
   - Ou aller dans `Applications` → `Agrégateur TV`

## 🛠️ Développement

### Prérequis
- Android Studio Arctic Fox ou plus récent
- JDK 17
- Android SDK 34
- Kotlin 1.9.10

### Compilation

```bash
# Cloner le projet
git clone https://github.com/lumina-ooo/tv-aggregator.git
cd tv-aggregator

# Rendre gradlew exécutable
chmod +x gradlew

# Construire l'APK debug
./gradlew assembleDebug

# Construire l'APK release
./gradlew assembleRelease

# Lancer les tests
./gradlew test

# Installer sur un appareil connecté
./gradlew installDebug
```

### Structure du Projet

```
app/src/main/java/com/lumina/tvaggregator/
├── data/               # Modèles de données et repository
├── ui/                 # Composants Jetpack Compose TV
│   ├── components/     # Composants réutilisables
│   ├── screens/        # Écrans principaux
│   └── theme/          # Thème Material 3 pour TV
├── viewmodel/          # ViewModels MVVM
├── navigation/         # Navigation Compose
└── util/               # Utilitaires
```

## 🎨 Technologies Utilisées

- **Kotlin** - Langage principal
- **Jetpack Compose for TV** - Interface utilisateur moderne
- **Material 3 for TV** - Design system optimisé TV
- **Navigation Compose** - Navigation entre écrans
- **ViewModel & StateFlow** - Architecture MVVM
- **Coil** - Chargement d'images
- **Leanback** - Support Android TV

## 📋 Configuration CI/CD

Le projet utilise GitHub Actions pour:
- ✅ Tests automatisés sur chaque push
- 🔨 Build d'APK sur main
- 🚀 Release automatique sur tags `v*`

```bash
# Créer une nouvelle release
git tag v1.0.0
git push origin v1.0.0
```

## 🔧 Dépannage

### L'app ne s'installe pas
- Vérifiez que les sources inconnues sont activées
- Assurez-vous que l'APK n'est pas corrompu
- Redémarrez votre Android TV

### Impossible d'ouvrir une plateforme
- L'app vous redirigera vers le Play Store pour installation
- Certaines apps peuvent ne pas être disponibles dans votre région

### Navigation difficile avec la télécommande
- Utilisez les flèches directionnelles pour naviguer
- Bouton OK/Entrée pour sélectionner
- Bouton Retour pour revenir en arrière

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Fork le projet
2. Créez votre branche feature (`git checkout -b feature/amazing-feature`)
3. Commitez vos changes (`git commit -m 'Add amazing feature'`)
4. Push sur la branche (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🙏 Remerciements

- [Jetpack Compose for TV](https://developer.android.com/jetpack/compose/tv) - Framework UI
- [Material 3](https://m3.material.io/) - Design system
- [Android Leanback](https://developer.android.com/training/tv/start) - Support TV

## 📞 Support

Pour toute question ou problème :
- 🐛 [Issues GitHub](https://github.com/lumina-ooo/tv-aggregator/issues)
- 📧 Contact: [support@lumina.ooo](mailto:support@lumina.ooo)

---

*Fait avec ❤️ pour la communauté francophone Android TV*