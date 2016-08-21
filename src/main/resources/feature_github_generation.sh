#/bin/bash
echo "Génération du fichier de features pour les releases GitHub"
echo "Release GitHub : $TRAVIS_BRANCH"
cd template/
# Remplacement de ${project.version} par le n° de release GitHub
sed -i -e "s/\${project.version}/$TRAVIS_BRANCH/g" feature.xml

sed -i -e "s/v\${project.version}/$TRAVIS_BRANCH/g" README.md
# Push sur Master
cd ..
git add README.md
git commit -m "Mise à jour des versions"
git push