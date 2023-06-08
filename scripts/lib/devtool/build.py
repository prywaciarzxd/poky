# Development tool - build command plugin
#
# Copyright (C) 2014-2015 Intel Corporation
#
# SPDX-License-Identifier: GPL-2.0-only
#
"""Devtool build plugin"""

import os
import bb
import re
import logging
import argparse
import tempfile
from devtool import exec_build_env_command, setup_tinfoil, check_workspace_recipe, DevtoolError
from devtool import parse_recipe

logger = logging.getLogger('devtool')


def _set_file_values(fn, values):
    remaining = list(values.keys())

    def varfunc(varname, origvalue, op, newlines):
        newvalue = values.get(varname, origvalue)
        remaining.remove(varname)
        return (newvalue, '=', 0, True)

    with open(fn, 'r') as f:
        (updated, newlines) = bb.utils.edit_metadata(f, values, varfunc)

    for item in remaining:
        updated = True
        newlines.append('%s = "%s"' % (item, values[item]))

    if updated:
        with open(fn, 'w') as f:
            f.writelines(newlines)
    return updated

def _get_build_tasks(config):
    tasks = config.get('Build', 'build_task', 'populate_sysroot,packagedata').split(',')
    return ['do_%s' % task.strip() for task in tasks]


def build(args, config, basepath, workspace):
    """Entry point for the devtool 'build' subcommand"""
    
    recipenames = " ".join(args.recipename.split(","))  # Łączymy argumenty przekazane jako lista w jeden ciąg znaków
    all_recipes = []
    for recipename in recipenames.split():
        if "_" in recipename:
            version = recipename.split("_")[1]
            recipename = recipename.split("_")[0]
            full_name = recipename + "_" + version
            all_recipes.append({full_name: {'version': version, 'recipename': recipename, 'full_name': full_name}})
        else:
            full_name = recipename
            all_recipes.append({full_name: {'version': '', 'recipename': recipename, 'full_name': full_name}})
    
    
    tmp = []
    
    for recipe in all_recipes:
        for key in recipe.keys():
            if "_" not in key:
                for workspace_key in workspace.keys():
                    if workspace_key.startswith(key):
                        tmp.append({workspace_key.split("_")[0]: workspace_key.split("_")[1]})

    

    najwyzsze_wersje = {}  # Słownik przechowujący najwyższe wersje dla każdego klucza

    for slownik in tmp:
        for klucz, wartosc in slownik.items():
            if klucz not in najwyzsze_wersje or wartosc > najwyzsze_wersje[klucz]:
                najwyzsze_wersje[klucz] = wartosc

    slowniki_najwyzszych = [{klucz: wartosc} for klucz, wartosc in najwyzsze_wersje.items()]
    
    for recipe in all_recipes:
        for key in recipe.keys():
            for slownik in slowniki_najwyzszych:
                for max_key, max_value in slownik.items():
                    if max_key == key:
                        recipe[key]['version'] = max_value
    

         
    for recipe in all_recipes:
        for key, value in recipe.items():
            if "_" in value['full_name']:
                check_workspace_recipe(workspace, key, bbclassextend=True)
  

    tinfoil = setup_tinfoil(config_only=False, basepath=basepath)
    try:
        rd = []
        for recipe in all_recipes:
            for value in recipe.values():
                rd.append(parse_recipe(config, tinfoil, value['recipename'], appends=True, filter_workspace=False))
        if not rd:
            return 1
        for rd in rd:
            deploytask = ('do_deploy' in rd.getVar('__BBTASKS'))
            devtoolconf = (os.path.join(rd.getVar('TMPDIR'), 'devtool-tmp.conf'))
        
        for recipe in all_recipes:
            for value in recipe.values():
                some_string_check = f'PREFERRED_VERSION_{value["recipename"]}='
                some_string = some_string_check + '"' + value['version'] + '"'
                with open(devtoolconf, 'r+') as file:
                    file_content = file.read()
                    if some_string not in file_content:
                        file.write(some_string + "\n")
                    
    finally:
        
        tinfoil.shutdown()
        

    if args.clean:
        # use clean instead of cleansstate to avoid messing things up in eSDK
        build_tasks = ['do_clean']
    else:
        build_tasks = _get_build_tasks(config)
        if deploytask:
            build_tasks.append('do_deploy')
            
    try:
        bbargs = []
        for task in build_tasks:
            if any(recipename.endswith('-native') and 'package' in task for recipename in recipenames):
                continue
            for recipe in all_recipes:
                for value in recipe.values():
                    bbargs.append('%s:%s' % (value["recipename"], task))
        exec_build_env_command(config.init_path, basepath, 'bitbake -R %s %s' % (devtoolconf, ' '.join(bbargs)), watch=True)
    except bb.process.ExecutionError as e:
        # We've already seen the output since watch=True, so just ensure we return something to the user
        return e.exitcode

    with open(devtoolconf, 'w') as file:
            file.truncate(0)
    return 0


def register_commands(subparsers, context):
    """Register devtool subcommands from this plugin"""
    parser_build = subparsers.add_parser('build', help='Build a recipe',
                                         description='Builds the specified recipe using bitbake (up to and including %s)' % ', '.join(_get_build_tasks(context.config)),
                                         group='working', order=50)
    parser_build.add_argument('recipename', help='Recipe to build')
    parser_build.add_argument('-s', '--disable-parallel-make', action="store_true", help='Disable make parallelism')
    parser_build.add_argument('-c', '--clean', action='store_true', help='clean up recipe building results')
    parser_build.set_defaults(func=build)
