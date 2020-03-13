# Copyright (C) 2014 Khem Raj <raj.khem@gmail.com>
# Released under the MIT license (see COPYING.MIT for the terms)

require musl.inc
inherit linuxloader

SRCREV = "040c1d16b468c50c04fc94edff521f1637708328"

BASEVER = "1.2.0"

PV = "${BASEVER}+git${SRCPV}"

# mirror is at git://github.com/kraj/musl.git

SRC_URI = "git://git.musl-libc.org/musl \
           file://0001-Make-dynamic-linker-a-relative-symlink-to-libc.patch \
           file://0002-ldso-Use-syslibdir-and-libdir-as-default-pathes-to-l.patch \
          "

S = "${WORKDIR}/git"

PROVIDES += "virtual/libc virtual/libiconv virtual/libintl virtual/crypt"

DEPENDS = "virtual/${TARGET_PREFIX}binutils \
           virtual/${TARGET_PREFIX}gcc \
           libgcc-initial \
           linux-libc-headers \
           bsd-headers \
           libssp-nonshared \
          "
GLIBC_LDSO = "${@get_glibc_loader(d)}"

export CROSS_COMPILE="${TARGET_PREFIX}"

LDFLAGS += "-Wl,-soname,libc.so"

# When compiling for Thumb or Thumb2, frame pointers _must_ be disabled since the
# Thumb frame pointer in r7 clashes with musl's use of inline asm to make syscalls
# (where r7 is used for the syscall NR). In most cases, frame pointers will be
# disabled automatically due to the optimisation level, but append an explicit
# -fomit-frame-pointer to handle cases where optimisation is set to -O0 or frame
# pointers have been enabled by -fno-omit-frame-pointer earlier in CFLAGS, etc.
CFLAGS_append_arm = " ${@bb.utils.contains('TUNE_CCARGS', '-mthumb', '-fomit-frame-pointer', '', d)}"

CONFIGUREOPTS = " \
    --prefix=${prefix} \
    --exec-prefix=${exec_prefix} \
    --bindir=${bindir} \
    --libdir=${libdir} \
    --includedir=${includedir} \
    --syslibdir=${base_libdir} \
"

do_configure() {
	${S}/configure ${CONFIGUREOPTS}
}

do_compile() {
	oe_runmake
}

do_install() {
	oe_runmake install DESTDIR='${D}'

	install -d ${D}${bindir}
	rm -f ${D}${bindir}/ldd ${D}${GLIBC_LDSO}
	lnr ${D}${libdir}/libc.so ${D}${bindir}/ldd
	lnr ${D}${libdir}/libc.so ${D}${GLIBC_LDSO}
}

PACKAGES =+ "${PN}-glibc-compat"

FILES_${PN}-glibc-compat += "${GLIBC_LDSO}"
FILES_${PN}-staticdev = "${libdir}/libc.a"
FILES_${PN}-dev =+ "${libdir}/libcrypt.a ${libdir}/libdl.a ${libdir}/libm.a \
                    ${libdir}/libpthread.a ${libdir}/libresolv.a \
                    ${libdir}/librt.a ${libdir}/libutil.a ${libdir}/libxnet.a \
                   "

RDEPENDS_${PN}-dev += "linux-libc-headers-dev bsd-headers-dev libssp-nonshared-staticdev"
RPROVIDES_${PN}-dev += "libc-dev virtual-libc-dev"
RPROVIDES_${PN} += "ldd libsegfault rtld(GNU_HASH)"

LEAD_SONAME = "libc.so"
INSANE_SKIP_${PN}-dev = "staticdev"
