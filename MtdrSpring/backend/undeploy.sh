echo delete frontend deployment and service...
kubectl -n mtdrworkshop delete deployment todolistapp-springboot-deployment
kubectl -n mtdrworkshop delete service todolistapp-springboot-service
kubectl -n mtdrworkshop delete service todolistapp-backend-router
