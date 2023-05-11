SUMMARY = "Vi IMproved - enhanced vi editor"
HOMEPAGE = "https://www.vim.org"
SECTION = "console/editors"
LICENSE = "Vim"

LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690 \
                    file://${WORKDIR}/vim74/src/version.c;beginline=1;endline=28;md5=7590e0c2b7e71ed5d47f5f5b66c52658"

SRC_URI = "https://github.com/vim/vim/archive/v8.2.0000.tar.gz"

SRC_URI[md5sum] = "319ba0ac832a2151a8492c8445fc1e9c"
SRC_URI[sha256sum] = "8a72323817210daf40abe545bdf7637591b9b541a0fb3560baed76e436132dba"

S = "${WORKDIR}/vim-8.2.000"

inherit autotools

FILES_${PN} += "${datadir}/vim/vim82"


