#!/bin/bash

# --- CONFIGURATION ---
CHEMIN_TOMCAT="/home/think/tomcat"
CHEMIN_SERVLET="$CHEMIN_TOMCAT/lib/servlet-api.jar"
CHEMIN_GSON="libs/gson-2.10.1.jar"
DOSSIER_TEMP="build_tmp"

echo "--------------------------------------------------"
echo "🧹 [1/4] Nettoyage..."
echo "--------------------------------------------------"
rm -rf bin $DOSSIER_TEMP framework.jar
mkdir -p bin
mkdir -p $DOSSIER_TEMP

echo "--------------------------------------------------"
echo "⚙️ [2/4] Compilation du Framework..."
echo "--------------------------------------------------"

SOURCES=$(find . -name "*.java" \
    -not -path "./libs/*" \
    -not -path "./build_tmp/*" \
    -not -path "./bin/*" \
    -not -path "*/.*/*")

echo "Fichiers à compiler :"
echo "$SOURCES"
echo "--------------------------------------------------"

javac -parameters -cp "$CHEMIN_SERVLET:$CHEMIN_GSON" -d bin $SOURCES

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation !"
    exit 1
fi
echo "✅ Fichiers .class générés dans bin/"

echo "--------------------------------------------------"
echo "📦 [3/4] Préparation du fat jar..."
echo "--------------------------------------------------"

cp -r bin/* $DOSSIER_TEMP/

cd $DOSSIER_TEMP
jar -xf "../$CHEMIN_GSON"
cd ..

rm -rf $DOSSIER_TEMP/META-INF/*.SF
rm -rf $DOSSIER_TEMP/META-INF/*.DSA
rm -rf $DOSSIER_TEMP/META-INF/*.RSA

echo "✅ Contenu fusionné"

echo "--------------------------------------------------"
echo "🎁 [4/4] Création du fat jar framework.jar..."
echo "--------------------------------------------------"
jar -cf framework.jar -C $DOSSIER_TEMP .

rm -rf $DOSSIER_TEMP

echo "🎉 framework.jar prêt !"