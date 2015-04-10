# -*- mode: ruby -*-
# vi: set ft=ruby :

# https://docs.vagrantup.com
Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.vm.provision "shell", path: "Vagrant-bootstrap.sh"
end
