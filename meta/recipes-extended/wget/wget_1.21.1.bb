SUMMARY = "GNU Wget is a free utility for non-interactive downloading of files from the web"
DESCRIPTION = "GNU Wget is a free utility for non-interactive downloading of files from the web. It supports HTTP, HTTPS, and FTP protocols, as well as retrieval through HTTP proxies."
HOMEPAGE = "https://www.gnu.org/software/wget/"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "https://ftp.gnu.org/gnu/wget/wget-1.21.1.tar.gz"
SRC_URI[md5sum] = "b939ee54eabc6b9b0a8d5c03ace879c9"
SRC_URI[sha256sum] = "59ba0bdade9ad135eda581ae4e59a7a9f25e3a4bde6a5419632b31906120e26e"

S = "${WORKDIR}/wget-1.21.1"

inherit autotools

do_configure:prepend() {
    aclocal -I m4
    autoheader
}

EXTRA_OECONF += "--disable-debug"

FILES_${PN} += "/usr/bin/wget"
