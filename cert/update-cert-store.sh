#!/bin/sh

dir=$(dirname $0)

keytool \
	-importcert \
	-v \
	-trustcacerts \
	-alias 0d86a8c1 \
	-file $dir/imgshr-space-zertifikatskette.pem \
	-keystore $dir/../app/src/main/assets/net.orgizm.imgshr.bks \
	-storetype BKS \
	-providerclass org.bouncycastle.jce.provider.BouncyCastleProvider \
	-providerpath $dir/bcprov-jdk14-166.jar \
	-storepass ahw0Iewiefei6jee
