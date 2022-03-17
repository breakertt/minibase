import os
import traceback
import shutil

minibase_ref1 = r"Minibase/target/minibase-1.0.0-jar-with-dependencies.jar"
minibase_ref2 = r"minibase-1.0.2-jar-with-dependencies.jar"
test_dir = r"Minibase/data/evaluation_extra/extra_02"

db_dir = os.path.join(test_dir, "db")
input_dir = os.path.join(test_dir, "input")
ref1_output_dir = os.path.join(test_dir, "ref1_output")
ref2_output_dir = os.path.join(test_dir, "ref2_output")

try:
    shutil.rmtree(ref1_output_dir)
except Exception:
    traceback.print_exc()
try:
    shutil.rmtree(ref2_output_dir)
except:
    traceback.print_exc()
try:
    os.makedirs(ref1_output_dir)
except OSError:
    traceback.print_exc()
try:
    os.makedirs(ref2_output_dir)
except OSError:
    traceback.print_exc()

input_files = [f for f in os.listdir(input_dir) if os.path.isfile(os.path.join(input_dir, f)) and f[-4:] == ".txt"]

for input_file_base in input_files:
    output_file =  input_file_base[:-4] + ".csv"
    ref1_output_file = os.path.join(ref1_output_dir, output_file)
    ref2_output_file = os.path.join(ref2_output_dir, output_file)
    input_file = os.path.join(input_dir, input_file_base)
    ref1_cmd = "java -cp {} ed.inf.adbs.minibase.Minibase {} {} {}".format(minibase_ref1, db_dir, input_file, ref1_output_file)
    ref2_cmd = "java -cp {} ed.inf.adbs.minibase.Minibase {} {} {}".format(minibase_ref2, db_dir, input_file, ref2_output_file)
    print("Execute ref1 on " + input_file_base)
    os.system(ref1_cmd)
    print("Execute ref2 on " + input_file_base)
    os.system(ref2_cmd)
    with open(ref1_output_file, 'r') as f:
        ref1_output_content = f.readlines()
        ref1_output_content.sort()
        ref1_output_content = list(map(lambda x: x.rstrip(), ref1_output_content))
    with open(ref2_output_file, 'r') as f:
        ref2_output_content = f.readlines()
        ref2_output_content.sort()
        ref2_output_content = list(map(lambda x: x.rstrip(), ref2_output_content))
    print(ref1_output_content)
    print(ref2_output_content)
    if ref1_output_content != ref2_output_content:
        raise Exception("Not match")

#try:
#    shutil.rmtree(ref1_output_dir)
#except Exception:
#    traceback.print_exc()
#try:
#    shutil.rmtree(ref2_output_dir)
#except:
#    traceback.print_exc()
