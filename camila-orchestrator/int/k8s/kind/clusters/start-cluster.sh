#!/usr/bin/env bash

set -o errexit # Exit on error. Append "|| true" if you expect an error.
set -o errtrace # Exit on error inside any functions or subshells.
set -o nounset # Do not allow use of undefined vars. Use ${VAR:-} to use an undefined VAR
if [[ "${debug:-}" == "true" ]]; then set -o xtrace; fi  # enable debug mode.

SEPARATOR="\n ################################################## \n"

cd "$(dirname "$0")"

echo -e "${SEPARATOR} 🚢 create a k8s cluster. ${SEPARATOR}"
# https://kind.sigs.k8s.io/
# https://kind.sigs.k8s.io/docs/user/quick-start/#creating-a-cluster

kind create cluster --config kind-cluster-config.yml
kind get clusters
kubectl cluster-info --context kind-kind-cluster


echo -e "${SEPARATOR} 🗑️ Init local registry. ${SEPARATOR}"
# https://kind.sigs.k8s.io/docs/user/local-registry/
docker run -d --rm -p "5000:5000" --network kind --name "kind-registry" registry:3


echo -e "${SEPARATOR} 🔗 Update the nodes to use the local registry. ${SEPARATOR}"
REGISTRY_DIR="/etc/containerd/certs.d/_default"
for node in $(kind get nodes --name kind-cluster); do
  docker exec "${node}" mkdir -p "${REGISTRY_DIR}"
  cat <<EOF | docker exec -i "${node}" cp /dev/stdin "${REGISTRY_DIR}/hosts.toml"
[host."http://kind-registry:5000"]
  capabilities = ["pull", "resolve"]
  skip_verify = true
EOF
done

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: local-registry-hosting
  namespace: kube-public
data:
  localRegistryHosting.v1: |
    host: "kind-registry:5000"
    help: "https://kind.sigs.k8s.io/docs/user/local-registry/"
EOF


echo -e "${SEPARATOR} 🌐 configure ingress controller (NGINX). ${SEPARATOR}"
# https://kind.sigs.k8s.io/docs/user/ingress/

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s


echo -e "${SEPARATOR} 🚀 install knative. ${SEPARATOR}"
# https://knative.dev/blog/articles/set-up-a-local-knative-environment-with-kind/
# https://knative.dev/docs/install/yaml-install/serving/install-serving-with-yaml/#prerequisites

kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.18.0/serving-crds.yaml
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.18.0/serving-core.yaml
kubectl apply -f https://github.com/knative/net-kourier/releases/download/knative-v1.18.0/kourier.yaml
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.18.0/serving-hpa.yaml

## By default, the Kourier service is set to be of type LoadBalancer. On local machines, this type doesn’t work
kubectl patch svc kourier \
  --namespace kourier-system \
  --type='json' \
  --patch '[{"op":"replace","path":"/spec/type","value":"NodePort"},{"op":"replace","path":"/spec/ports/0/nodePort","value":31080},{"op":"replace","path":"/spec/ports/1/nodePort","value":31443}]'

kubectl patch configmap/config-network \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"ingress-class":"kourier.ingress.networking.knative.dev"}}'

kubectl describe configmap/config-network --namespace knative-serving | grep kourier.ingress.networking.knative.dev

kubectl patch configmap/config-domain \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"127.0.0.1.sslip.io":""}}'

kubectl describe configmap/config-domain --namespace knative-serving | grep 127.0.0.1

kubectl get pods --namespace knative-serving
kubectl get pods --namespace kourier-system
kubectl --namespace kourier-system get service kourier


echo -e "${SEPARATOR} 📊 install metric server. ${SEPARATOR}"
# https://github.com/kubernetes-sigs/metrics-server/
# full list of Metrics Server configuration flags: docker run --rm registry.k8s.io/metrics-server/metrics-server:v0.6.0 --help

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl patch -n kube-system deployment metrics-server --type=json \
  -p '[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
#kubectl top nodes
