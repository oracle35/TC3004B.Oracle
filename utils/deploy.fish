argparse 'github' -- $argv

function fail_with_msg
    echo $argv 
    exit 1
end

if set -q _flag_github
    set -l git_ref (git rev-parse --verify origin/nix)
    or fail_with_msg "origin/nix does not exist. Add origin url maybe? Or origin is pointing to the wrong repo."

    set -l image "$IMAGE_NAME:$git_ref"
    echo "checking image exists ($image)"
    docker manifest inspect $image >/dev/null
    or fail_with_msg "image doesn't exist. Check github actions status?"

    echo "Deploying image to cluster..."
    kubectl set image deployment.apps/todolistapp-springboot-deployment todolistapp-springboot=$image
    or fail_with_msg "Kubernetes deployment failed..."
end

