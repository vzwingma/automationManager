#/bin/bash
TRAVIS_BRANCH=1.2.0-RC1
echo "Génération du fichier de features pour les releases GitHub"
echo "Release GitHub : $TRAVIS_BRANCH"
cd template/
# Remplacement de ${project.version} par le n° de release GitHub
sed -i -e "s/\${project.version}/$TRAVIS_BRANCH/g" feature.xml
# Todo : Doit être recommité sur master
sed -i -e "s/v\${project.version}/$TRAVIS_BRANCH/g" README.md


echo ""
echo ""
echo "************** feature.xml ****************"
cat feature.xml
echo ""
echo ""
echo "************** README.md ****************"
cat README.md