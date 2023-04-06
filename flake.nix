{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-22.11";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config.allowUnfreePredicate = pkg: builtins.elem (nixpkgs.lib.getName pkg) [
              "ngrok"
            ];
          };
        in
        {
          formatter = pkgs.nixpkgs-fmt;
          packages.jar = with pkgs;
            let
              manifest = pkgs.writeText "Manifest.txt" ''
                Main-Class: com.orijtech.btcpay.CLI
              '';
            in
            stdenvNoCC.mkDerivation {
              name = "btcpay.jar";
              src = ./.;

              buildPhase = ''
                mkdir classes
                classpath="lib/jackson-core-2.13.4.jar:lib/jackson-databind-2.13.4.2.jar:lib/jackson-annotations-2.13.4.jar"
                ${jdk}/bin/javac -classpath $classpath -d classes src/com/orijtech/btcpay/*
                ${jdk}/bin/jar -cfm btcpay.jar ${manifest} -C classes .
              '';

              installPhase = ''
                mkdir -p $out
                mv btcpay.jar $out/
              '';
            };
          devShells.default = with pkgs; mkShell
            {
              packages = [
                ngrok
                jdk
              ];
            };
        });
}
