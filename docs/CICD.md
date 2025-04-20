# CI/CD

Currently we use **GitHub Actions** for our CI/CD Pipeline. Our build and testing
processes are powered by Nix.

## Nix overview
Nix is a build system focused on reproducibility. Using the Nix language, a functional,
dynamic programming language, you define **derivations** which have files as input
and produce an output, normally compiled or bundled code. All the inputs are fixed
with hashes such that any Nix derivation will produce the exact same output,
byte for byte, **regardless of external environment**.

We take advantage of this to lock the Maven dependencies for our backend and
the NPM dependencies on the frontend. For example, if react were to update its
version and somehow break the entire project, we can rollback the package lock,
as well as the Nix package definition and guarantee that it will always produce
a good build. That cannot always be guaranteed building normally as node_modules
is not tracked by git.

## Our project and Bash
We use Bash scripting in the Nix build process and outside it as well.
All Nix derivations use 'phases' and 'hooks', basically glorified Bash functions,
to run the required tasks to build code. We expand on these existing hooks for
our specific use cases. Here's an example:

To build our backend, we use a Nix project called `mvn2nix` to make a Nix derivation
that contains all of the Maven dependencies required to build it. We need to tell
Maven to use this particular repository and to not download anything from the internet
(to ensure reproducibility). We do this with the following Bash commands:
```bash
mkdir -p target
ln -s ${todoapp-frontend} ./target/frontend
mvn package --offline -Dmaven.repo.local=${mavenRepository}
```
We create a `target` folder, doing nothing if it already exists with the `-p` flag,
link our frontend (another Nix derivation) into the folder that Maven expects our
frontend to be in and finally create the jar with our custom repository.

For local development, we change the entrypoint on this package to a custom
script called `nix-run.sh`, which reads an environment specification `.env`
and exports the variables, which are then passed to the jar.

## The Nix build action

Our Nix workflow is defined in `.github/workflows/nix-build.yaml`.
It installs Nix, sets up a custom cache to speed up build times,
and builds the docker image for our app. The linting and testing process is
included in the package specification (currently only for the frontend) and as such
the build will fail in case the linter finds errors or a test fails. This makes the
action as a whole fail which will notify us via email and show a red X on the commit.

