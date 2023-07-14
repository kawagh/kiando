import pathlib
import re
from dataclasses import dataclass


@dataclass
class ChangeLog:
    title: str
    yyyymmdd: str


@dataclass
class ReleaseLog:
    version: str
    yyyymmdd: str


def main():
    changelogs_kt_file_path = (
        pathlib.Path(__file__).parent.parent
        / "app/src/main/java/jp/kawagh/kiando/ui/screens/ChangeLogScreen.kt"
    )
    assert changelogs_kt_file_path.exists(), "file not found"

    parsed_logs = []
    for l in changelogs_kt_file_path.read_text().split("\n"):
        if re.match(r"\s*Change.*", l):
            if len(str(l).split('"')) <= 1:
                continue
            title = l.split('"')[1]
            date = l.split(")")[-3].split("(")[-1]
            yyyymmdd_tokens = date.split(",")
            yyyymmdd = "".join(map(lambda x: str(int(x)).zfill(2), yyyymmdd_tokens))
            changelog = ChangeLog(title, yyyymmdd)
            parsed_logs.append(changelog)
        if re.match(r"\s*ReleaseLog.*", l):
            if len(str(l).split('"')) <= 1:
                continue
            version = l.split('"')[1]
            date = l.split(")")[-3].split("(")[-1]
            yyyymmdd_tokens = date.split(",")
            yyyymmdd = "".join(map(lambda x: str(int(x)).zfill(2), yyyymmdd_tokens))
            releaselog = ReleaseLog(version, yyyymmdd)
            parsed_logs.append(releaselog)
    print(*parsed_logs,sep="\n")
    # TODO generate CHANGELOG_.md


if __name__ == "__main__":
    main()
