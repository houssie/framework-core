#!/bin/bash

# --- CONFIGURATION ---
CHEMIN_TOMCAT="/home/think/tomcat"

echo "--------------------------------------------------"
echo "🧹 [1/3] Nettoyage du dossier bin..."
echo "--------------------------------------------------"
mkdir -p bin
rm -rf bin/*

echo "--------------------------------------------------"
echo "⚙️ [2/3] Compilation du Framework en Java Pur..."
echo "--------------------------------------------------"
# 💡 EN PASSANT LE CHEMIN RELATIF "framework/...", 
# javac SAIT qu'il doit créer un dossier "framework" dans "bin/" !
javac -cp "$CHEMIN_TOMCAT/lib/servlet-api.jar" -d bin \
    framework/annotation/Controller.java \
    framework/annotation/Url.java \
    framework/Mapping.java \
    framework/FrontServlet.java

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation du framework !"
    exit 1
fi
echo "✅ Fichiers .class générés avec succès."

echo "--------------------------------------------------"
echo "📦 [3/3] Création du fichier framework.jar..."
echo "--------------------------------------------------"
# On cible proprement le dossier "framework" fraîchement créé par javac dans bin
jar -cvf framework.jar -C bin framework