import os

import re
import json

# PebblesCrates to SkiesCrates converter script!
# This script will convert crate files for use with the SkiesCrates mod.
# This script is not perfect and may not convert everything correctly.

# 1. Place your PebblesCrates .json files in the 'input' directory.
# 2. Run this script to convert the files to into SkiesCrates files.
# 3. The converted .json files will be placed in the 'output' directory.
#   - The 'output' directory will contain two subdirectories: 'crates' and 'keys'.
#       - The 'crates' directory will contain the converted crate files.
#       - The 'keys' directory will contain the converted key files based on the keys in the original crate files.

input_dir = 'input'
output_crates_dir = 'output/crates'
output_keys_dir = 'output/keys'

def convert_color(match_obj):
    if match_obj.group() is not None:
        match_result = match_obj.group().replace('&', '').replace('§', '')
        match match_result:
            case '0': return '<black>'
            case '1': return '<dark_blue>'
            case '2': return '<dark_green>'
            case '3': return '<dark_aqua>'
            case '4': return '<dark_red>'
            case '5': return '<dark_purple>'
            case '6': return '<gold>'
            case '7': return '<gray>'
            case '8': return '<dark_gray>'
            case '9': return '<blue>'
            case 'a': return '<green>'
            case 'b': return '<aqua>'
            case 'c': return '<red>'
            case 'd': return '<light_purple>'
            case 'e': return '<yellow>'
            case 'f': return '<white>'
            case 'k': return '<obf>'
            case 'l': return '<b>'
            case 'm': return '<st>'
            case 'n': return '<u>'
            case 'o': return '<i>'
            case 'r': return '<reset>'
            case _:
                if match_result.startswith('#'):
                    return f"<{match_result}>"
                else:
                    print("Unsupported Color Found: " + match_result)
    return None


def parse_color(input):
    return (re.sub(r'[&§][0-9a-fklmnor]|[&§]#([0-9a-fA-F]{6})', convert_color, input)
            .replace("%player_name%", "%player%"))

def parse_commands(input):
    return input.replace("{player_name}", "%player%")

def parse_reward_string(input):
    return parse_color(input).replace("{player_name}", "%player%").replace("{chance}", "%reward_percent%")

def parse_rewards(rewards):
    rewards_map = {}

    for i, reward in enumerate(rewards):
        name = parse_color(reward['name'])
        print(f"Processing Reward #{i}, named: {name}")
        reward_map = {
            "type": "COMMAND_CONSOLE",
            "name": name,
            "weight": reward['chance']
        }

        display_node = {
            "item": reward['material'],
            "name": name
        }

        if 'lore' in reward:
            display_node['lore'] = list(map(parse_color, reward['lore']))

        if 'nbt' in reward:
            nbt_input = parse_color(reward['nbt'])

            # Safely quote keys
            nbt_input = re.sub(r'([{,])\s*([a-zA-Z0-9_]+)\s*:', r'\1"\2":', nbt_input)

            # Convert single-quoted strings to double-quoted, escaping any inner double quotes
            def parse_single_to_double(m):
                inner = m.group(1).replace('"', '\\"')
                return f'"{inner}"'

            nbt_input = re.sub(r"'([^']*)'", parse_single_to_double, nbt_input)

            nbt_input = nbt_input.replace("\"CustomModelData\"", "\"minecraft:custom_model_data\"")
            nbt_input = nbt_input.replace("\"Name\"", "\"minecraft:item_name\"")
            nbt_input = nbt_input.replace("\"Lore\"", "\"minecraft:lore\"")
            nbt_input = nbt_input.replace("\"Unbreakable\":1", "\"minecraft:unbreakable\":{}")
            nbt_input = nbt_input.replace("\"Name\"", "\"minecraft:item_name\"")
            nbt_input = nbt_input.replace("\"Name\"", "\"minecraft:item_name\"")
            nbt_input = nbt_input.replace("\"Name\"", "\"minecraft:item_name\"")

            try:
                parsed = json.loads(nbt_input)

                if "species" in parsed and "aspects" in parsed:
                    new_lore = {"cobblemon:pokemon_item": parsed}
                    parsed = new_lore

                if "display" in parsed:
                    display = parsed["display"]
                    if "minecraft:item_name" in display:
                        parsed["minecraft:item_name"] = display["minecraft:item_name"]
                    if "minecraft:lore" in display:
                        parsed["minecraft:lore"] = display["minecraft:lore"]

                    # Remove "display" from parsed
                    del parsed["display"]

                display_node['nbt'] = parsed
            except json.JSONDecodeError as e:
                print(f"JSON Error - Unable to decode NBT: {e}\nRaw Input: {nbt_input}")
                display_node['nbt'] = nbt_input  # Fallback to the raw string


        reward_map['display'] = display_node
        reward_map['commands'] = list(map(parse_commands, reward['commands']))


        rewards_map[f"reward_{i}"] = reward_map
        print(f"Finished Processing Reward #{i}")

    return rewards_map


print("Iterating through the .json files in the 'input' directory and converting them...")
os.makedirs(input_dir, exist_ok=True)
for root, dirs, files in os.walk(input_dir):
    for filename in files:
        if filename.lower().endswith('.json'):
            crate_id = filename.replace('.json', '').replace(" ", "_").lower()
            key_id = crate_id + "_key"

            input_path = os.path.join(root, filename)

            output_crates_root = root.replace(input_dir, output_crates_dir)
            output_crates_path = os.path.join(output_crates_root, crate_id + ".json")

            output_keys_root = root.replace(input_dir, output_keys_dir)
            output_keys_path = os.path.join(output_keys_root, key_id + ".json")

            os.makedirs(os.path.dirname(output_crates_path), exist_ok=True)
            os.makedirs(os.path.dirname(output_keys_path), exist_ok=True)


            with open(input_path, 'r', encoding='utf-8') as f:
                data = json.load(f)


                if 'crateKey' in data:
                    key_node = {
                        'enabled': True,
                        'virtual': False,
                        'name': parse_color(data['crateKey']['name']),
                        'display': {
                            'item': data['crateKey']['material'],
                            'name': parse_color(data['crateKey']['name'])
                        }
                    }

                    if 'lore' in data['crateKey']:
                        key_node['display']['lore'] = list(map(parse_color, data['crateKey']['lore']))

                    if 'nbt' in data['crateKey']:
                        parsed_json = json.loads(parse_color(data['crateKey']['nbt']))
                        key_node['display']['nbt'] = json.dumps(parsed_json, indent=4)


                    with open(output_keys_path, 'w', encoding='utf-8') as o:
                        json.dump(key_node, o, indent=4, ensure_ascii=False)


                with open(output_crates_path, 'w', encoding='utf-8') as o:
                    node = {}

                    name = parse_color(data['crateName']) if 'crateName' in data else None

                    if name is not None:
                        node['name'] = name

                    node['display'] = {
                        'item': "minecraft:chest",
                        'name': name,
                        'lore': [],
                        'nbt': {}
                    }

                    node['unique'] = False
                    node['preview'] = "default"
                    node['animation'] = "csgo"
                    node['cost'] = {}
                    node['cooldown'] = -1
                    node['keys'] = {
                        key_id: 1
                    }
                    node['failure'] = {
                        "pushback": 0.5,
                        "sound": {
                            "sound": "minecraft:block.lava.extinguish",
                            "pitch": 1.0,
                            "volume": 0.5
                        }
                    }
                    node['block'] = {
                        "locations": [],
                        "hologram": {
                            "text": [
                                name
                            ],
                            "offset": {
                                "x": 0.0,
                                "y": 1.0,
                                "z": 0.0
                            },
                            "scale": {
                                "x": 1.0,
                                "y": 1.0,
                                "z": 1.0
                            },
                            "rotation": {
                                "x": 0.0,
                                "y": 0.0,
                                "z": 0.0
                            },
                            "billboard": "VERTICAL",
                            "background": {
                                "color": "000000",
                                "opacity": 50
                            },
                            "shadow": True,
                            "opacity": 1.0,
                            "update_rate": 100,
                            "view_distance": 50.0
                        },
                        "particles": ""
                    }

                    rewards = data['prize'] if 'prize' in data else None

                    if rewards is not None:
                        node['rewards'] = parse_rewards(data['prize'])


                    json.dump(node, o, indent=4, ensure_ascii=False)
