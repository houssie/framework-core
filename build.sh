#!/bin/bash

# --- CONFIGURATION ---
CHEMIN_TOMCAT="/home/think/tomcat"

echo "--------------------------------------------------"
echo "🧹 [1/3] Nettoyage..."
echo "--------------------------------------------------"
rm -rf bin
mkdir -p bin

echo "--------------------------------------------------"
echo "⚙️ [2/3] Compilation du Framework..."
echo "--------------------------------------------------"

# On liste les dossiers contenant du code pour aider javac
# Utiliser 'find' est le moyen le plus sûr de trouver tous les .java
SOURCES=$(find framework -name "*.java")

javac -cp "$CHEMIN_TOMCAT/lib/servlet-api.jar" -d bin $SOURCES

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation !"
    exit 1
fi
echo "✅ Fichiers .class générés dans bin/"

echo "--------------------------------------------------"
echo "📦 [3/3] Création du fichier framework.jar..."
echo "--------------------------------------------------"
jar -cvf framework.jar -C bin .

echo "🎉 Opération terminée : framework.jar est prêt !"