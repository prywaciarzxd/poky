SUMMARY = "Vi IMproved - enhanced vi editor"
HOMEPAGE = "https://www.vim.org"
SECTION = "console/editors"
LICENSE = "Vim"

LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${WORKDIR}/vim74/src/version.c;beginline=1;endline=28;md5=7590e0c2b7e71ed5d47f5f5b66c52658"

SRC_URI = "https://ftp.nluug.nl/pub/vim/unix/vim-7.4.tar.bz2"
SRC_URI[md5sum] = "607e135c559be642f210094ad023dc65"
SRC_URI[sha256sum] = "d0f5a6d2c439f02d97fa21bd9121f4c5abb1f6cd8b5a79d3ca82867495734ade"

S = "${WORKDIR}/vim-7.4.273"

inherit autotools

FILES_${PN} += "${datadir}/vim/vim74"

