#!/bin/sh

case $(uname) in
Linux)
  bin_abs_path=$(readlink -f $(dirname $0))
  ;;
*)
  bin_abs_path=$(cd $(dirname $0) ||exit ; pwd)
  ;;
esac

sh "$bin_abs_path"/stop.sh
sh "$bin_abs_path"/startup.sh
