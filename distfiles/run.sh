#!/usr/bin/env sh
# wue launcher (POSIX). Requires Java 17+ on PATH.
DIR="$(cd "$(dirname "$0")" && pwd)"
JVM_OPTS="-Djava.awt.headless=true"
case "$(uname -s)" in
  Darwin) JVM_OPTS="$JVM_OPTS -XstartOnFirstThread" ;;
esac

if ! command -v java >/dev/null 2>&1; then
  echo "wue: 'java' not found on PATH. Install Java 17+ and retry." >&2
  exit 1
fi

cd "$DIR" || exit 1
exec java $JVM_OPTS -jar wue.jar "$@"
