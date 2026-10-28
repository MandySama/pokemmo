# 城都剧情文本覆盖

用途：首徽章剧情显示；键为本机ROM原生 `bank:entry`。来源为本机官方安装 `D:/PokeMMO/data/mods/chinese-language-20251101.mod` 内 `custom-strings/ns_strings_zh-Hant_heartgold.xml`，根节点 region_id=4。保持原文及变量，仅将字面的反斜杠n转换成换行。

映射依据参考 `DpptTextBank.E1`：本章原生38对应canonical40；52..54对应canonical54..56；366/370对应373/377；535/537/542/550对应543/545/550/558。逐项通过ROM表长度与情节内容核对。简中同MOD文件含替换字符，不采用；本机HGSS汉化ROM与参考Fc字表不兼容，不能直接显示其解码乱码。

触发/分支/进度见服务端 `story/johto/first-badge.md`；本文件自身不改变进度。生成器只保存本章实际使用的文本，未接入完整地区覆盖。
