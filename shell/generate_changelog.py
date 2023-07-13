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

    texts = changelogs_kt_file_path.read_text()
    # TODO parse releaselog
    found = re.findall(r"\s.Change.*", texts)
    for l in found:
        # print(l)
        if len(str(l).split('"')) <= 1:
            continue
        title = str(l).split('"')[1]
        date = str(l).split(")")[-3].split("(")[-1]
        yyyymmdd_tokens = date.split(",")
        yyyymmdd = "".join(map(lambda x: str(int(x)).zfill(2), yyyymmdd_tokens))
        changelog = ChangeLog(title, yyyymmdd)
        print(changelog)

    # TODO generate CHANGELOG_.md

if __name__ == "__main__":
    main()
