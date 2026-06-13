#!/bin/bash

# --- CONFIGURATION ---
CHEMIN_TOMCAT="/home/think/tomcat"
# On définit le chemin vers la racine de ton code Java
RACINE_CODE="/home/think/framework-core/framework"

echo "--------------------------------------------------"
echo "🧹 [1/3] Nettoyage du dossier bin..."
echo "--------------------------------------------------"
mkdir -p bin
rm -rf bin/*

echo "--------------------------------------------------"
echo "⚙️ [2/3] Compilation du Framework en Java Pur..."
echo "--------------------------------------------------"
# On donne les chemins exacts vers tes fichiers en utilisant la variable RACINE_CODE
javac -cp "$CHEMIN_TOMCAT/lib/servlet-api.jar" -d bin \
    $RACINE_CODE/annotation/Controller.java \
    $RACINE_CODE/annotation/Url.java \
    $RACINE_CODE/Mapping.java \
    $RACINE_CODE/FrontServlet.java

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation du framework !"
    exit 1
fi
echo "✅ Fichiers .class générés avec succès dans le dossier bin/."

echo "--------------------------------------------------"
echo "📦 [3/3] Création du fichier framework.jar..."
echo "--------------------------------------------------"
cd bin
jar -cvf ../framework.jar *
cd ..

echo "🎉 Terminé ! Ton fichier framework.jar est prêt à la racine."