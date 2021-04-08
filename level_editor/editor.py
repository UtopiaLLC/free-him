# free-him Level Editor
# Yimeng Zeng, Tony Zhang, Brian Zhu

import json
import tkinter as tk
from tkinter import Label, Entry, Button, Checkbutton, OptionMenu
from tkinter.scrolledtext import ScrolledText
import numpy as np
from PIL import ImageTk, Image
import os

"""
todo:
grid resizing
add multiple targets*
larger text boxes for things with big inputs* <--- done
Images (wtf y arent they working)
Moving nodes around (delete connections in that case)* <--- done
make sure the children are correctly parsed in the output json*
should probably touch up on the controls
associating nodes with a particular target*
README after this is done
"""


# 0 = vacent/nothing selected
# 1 = target selected
# 2 = node selected
# 3 = deletion
# 4 = draw line
nothing_selected = 0
target_selected = 1
node_selected = 2
deletion = 3
draw_line = 4

empty_tile = 0
target_tile = 1
node_tile = 2
line_tile = 4

w = 1
h = 1

mode = 0
row_num = 13
column_num = 13
# 0 = empty tile
# 1 = target tile
# 2 = node tile
# 4 = line tile
grid = np.zeros((row_num, column_num))

menu_open = False
selected_node = None

cwd = None

def transform_world_to_screen(world_pos):
    x,y = world_pos
    v = np.asarray([x,y,1])
    M = np.asarray(
        [
            [np.sqrt(3*(h**2+w**2))/(4*w),    np.sqrt(3*(h**2+w**2))/(4*h),     0],
            [-np.sqrt(h**2+w**2)/(4*w),       np.sqrt(h**2+w**2)/(4*h),         h/2],
            [0,                               0,                                1]
        ]
    )
    Mv = M @ v
    return Mv[:2]

def transform_screen_to_world(screen_pos):
    x,y = screen_pos
    v = np.asarray([x,y,1])
    M = np.asarray(
        [
            [np.sqrt(3*(h**2+w**2))/(4*w),    np.sqrt(3*(h**2+w**2))/(4*h),     0],
            [-np.sqrt(h**2+w**2)/(4*w),       np.sqrt(h**2+w**2)/(4*h),         h/2],
            [0,                               0,                                1]
        ]
    )
    Minv = np.linalg.inv(M)
    Minvv = Minv @ v
    return Minvv[:2]

class Target:
    def __init__(self, contents=None):
        if contents == None:
            self.combos = set()
            self.pos = (0, 0)
            self.name = 'John Doe'
            self.suspicion = 1
            self.max_stress = 100
            self.neighbors = set()
            self.starting_stress = 0
            self.children = set()
            self.factnodes = set()
            # self.lines = set()  # a set with int duples (x, y)
            # self.target.factnodes = dict()
        else:
            self.name = contents['name']
            self.combos = {(combo['overwritten'], combo['summary'], int(combo['modified_dam']), combo['nodes'])
                           for combo in contents['combos']}
            self.pos = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.suspicion = int(contents['suspicion'])
            self.max_stress = int(contents['max_stress'])
            self.starting_stress = int(contents['starting_stress'])
            self.neighbors = set(contents['neighbors'])
            self.children = set(contents['children'])
            self.factnodes = {FactNode(contents = c) for c in contents['target.factnodes']}
            # self.lines = set(contents['lines'])
            # self.target.factnodes

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass
    
    def all_children(self):
        # TODO
        children = set()
        border = set() | self.children
        # while border

    def __str__(self):
        # TODO
        return ''


class FactNode:
    def __init__(self, pos=(0, 0), contents=None):
        '''
            contents is a dictionary containing factnode data
        '''
        if contents == None:
            self.name = 'Node'
            self.title = ''
            self.pos = pos
            self.children = set()
            # self.parent = None
            self.connection_to_parent = set()
            self.player_stress_dam = 0
            self.stress_dam = 0
            self.summary = ''
            self.contents = ''
            self.locked = True
            self.children = set()
        else:
            self.name = contents['name']
            self.title = contents['title']
            self.pos = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.children = {FactNode(contents=factnode) for factnode in contents['children']}
            # self.parent =
            self.connection_to_parent = {(int(con_route[0]), int(con_route[1])) for con_route in contents['connection_to_parent']}
            self.player_stress_dam = int(contents['player_stress_dam'])
            self.stress_dam = contents['stress_dam']
            self.summary = contents['summary']
            self.contents = contents['contents']
            self.locked = bool(contents['locked'])
            self.children = set(contents['children'])

    def update_parents(self, level):
        elements = level.target.factnodes | {level.target} 
        connection_tiles = set() | self.connection_to_parent
        border = {
            (self.pos[0],self.pos[1]-1),
            (self.pos[0],self.pos[1]+1),
            (self.pos[0]-1,self.pos[1]),
            (self.pos[0]+1,self.pos[1]) }
        while len(border) > 0:
            tile = border.pop()
            if tile not in connection_tiles:
                el = level.element_at(tile)
                if el and el in elements:
                    el.children.add(self)
                    elements.remove(el)
                continue
            connection_tiles.remove(tile)
            border |= {
                (tile[0],tile[1]-1),
                (tile[0],tile[1]+1),
                (tile[0]-1,tile[1]),
                (tile[0]+1,tile[1]) }
        for el in elements:
            el.children.delete(self)


    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass


class Level:
    def __init__(self, file_list=None):
        self.targets = set()
        if file_list == None:
            pass
            # self.target.factnodes = set()
        else:
            for file in file_list:
                jason = json.load(file)
                self.targets.add(Target(contents=jason))
            # self.target.factnodes = {FactNode(contents=cont) for cont in jason['target.factnodes']}
        self.active_element = None
        self.drawing = False  # self.drawing if drawing a FactNode path
        # self.view = None

    def add_view(self, view):
        self.view = view

    def element_at(self, pos):
        pos = (int(pos[0]), int(pos[1]))
        for target in self.targets:
            if pos == target.pos:
                return target
            for factnode in target.factnodes:
                if factnode.pos == pos:
                    return factnode
        return None

    def click(self, pos):
        if self.drawing:
            pass
        if self.active_element != None:
            self.active_element.close_gui()
        self.active_element = self.click(pos)
        if self.active_element != None:
            self.active_element.open_gui()

    def gen_json(self):
        pass


# 0 = vacent/nothing selected
# 1 = target selected
# 2 = node selected
# 3 = deletion
# 4 = draw line

# 0 = empty tile
# 1 = target tile
# 2 = node tile
# 4 = line tile
class Controwler:
    def __init__(self, level=None):
        self.level = level

    def add_object(self, event):
        global w
        global h
        global grid
        global column_num
        global row_num
        global menu_open
        global selected_node

        print(str(w) + ', ' + str(h))

        # row = (event.x - event.x % (w / row_num)) / row_num
        # column = (event.y - event.y % (int(h) / column_num)) / column_num
        new_x, new_y = transform_screen_to_world((event.x, event.y))
        print(str(new_x) + ', ' + str(new_y))
        row = int(new_x / (w/row_num))
        column = int(new_y / (h/column_num))

        if mode == node_selected:
            # if self.level.element_at((row, column)) == None:
            #     self.level.target.factnodes.add(FactNode(pos=(row, column)))
            selected_node = (row,column)
            # if menu_open:
            #     if grid[row][column] == empty_tile:
            #         self.level.target.factnodes.add(FactNode(pos=(row, column)))
            # elif selected_node:
            #     if (row,col) in self.level.element_at((selected_node)).connection_to_parent:
            #         self.level.element_at((selected_node)).connection_to_parent.remove((row,col))
            #     else:
            #         self.level.element_at((selected_node)).connection_to_parent.add((row,col))
        elif mode == deletion:
            if isinstance(self.level.element_at((row,column)), FactNode):
                for target in self.level.targets:
                    for f in target.factnodes:
                        if f.pos[0] == row and f.pos[1] == column:
                            target.factnodes.remove(f)
            # elif grid[row][column] == line_tile:
            #     for l in self.level.target.lines:
            #         if l.pos[0] == row and l.pos[1] == column:
            #             self.level.target.lines.remove(l)
        elif mode == draw_line:
            if grid[row][column] == empty_tile:
                #print(selected_node)
                el = self.level.element_at(selected_node)
                if (row,column) not in el.connection_to_parent:
                    el.connection_to_parent.add((row, column))
                    el.update_parents()
                else:
                    el.connection_to_parent.remove((row, column))
                    el.update_parents()

    def save_target(self, name, neighbours, susbision, sturess, maxitress, warudoposX, warudoposY, kangbous):
        targetfinder = [target for target in self.level.targets if target.name == name]
        if len(targetfinder) > 0:
            target = targetfinder[0]
            target.name = name
            target.neighbors = set(neighbours.split(","))
            target.suspicion = int(susbision)
            target.starting_stress = int(sturess)
            target.max_stress = int(maxitress)
            target.pos = (int(warudoposX), int(warudoposY))
            target.combos = set(kangbous.split(","))
        else:
            target = Target()
            target.name = name
            target.neighbors = set(neighbours.split(","))
            target.suspicion = int(susbision)
            target.starting_stress = int(sturess)
            target.max_stress = int(maxitress)
            target.pos = (int(warudoposX), int(warudoposY))
            target.combos = set(kangbous.split(","))
            self.level.targets.add(target)

    # Name/Title/World PositionX/Y/Player Stress Damage/Stress Damage/Summary/Contents/Locked
    def save_node(self, targetname, name, taitoru, warudoposX, warudoposY, platresmg, tresmg, Summary, Contents, Locked):
        
        tempdict = {
            'name': name,
            'title': taitoru,
            'pos': (int(warudoposX), int(warudoposY)),
            'children': set(),
            'player_stress_dam': int(platresmg),
            'stress_dam': int(tresmg),
            'summary': Summary,
            'contents': Contents,
            'locked': (Locked == 'T'),
            'connection_to_parent': set()
        }
        noude = FactNode(pos=(int(warudoposX), int(warudoposY)), contents=tempdict)
        
        print('target name queried: ' + name)
        print('target names extant: ' + str([target.name for target in self.level.targets]))
        print('search results: ' + str([target.name for target in self.level.targets if target.name == name]))

        [target for target in self.level.targets if target.name == name][0].factnodes.add(noude)


class View:

    def __init__(self, controwler, level):
        self.controwler = controwler
        self.level = level
        self.root = tk.Tk('main')
        self.canvas = tk.Canvas(self.root, height=1000, width=1000, bg='white')
        global w
        w = self.canvas.winfo_width()
        global h
        h = self.canvas.winfo_height()
        self.root.title('very very serious legit level editor')
        self.canvas.pack(fill=tk.BOTH, expand=True)
        self.canvas.bind('<Configure>', self.draw_everything)
        self.canvas.bind('<Button-1>', self.controwler.add_object)
        self.canvas.bind('<Button-3>', self.open_new_menu)
        # self.canvas.bind('<Key>', self.change_mode)
        self.canvas.bind('1', self.change_mode_1)
        self.canvas.bind('2', self.change_mode_2)
        self.canvas.bind('3', self.change_mode_3)
        self.canvas.bind('4', self.change_mode_4)
        self.canvas.bind('0', self.change_mode_0)
        self.canvas.focus_set()
        self.root.mainloop()
    
    
    def change_mode_0(self, event = None):
        global mode
        mode = 0
    
    def change_mode_1(self, event = None):
        global mode
        mode = 1
    
    def change_mode_2(self, event = None):
        global mode
        mode = 2
    
    def change_mode_3(self, event = None):
        global mode
        mode = 3
    
    def change_mode_4(self, event = None):
        global mode
        mode = 4

    def draw_everything(self, event=None, rows=row_num, columns=column_num):
        self.canvas.focus_set()
        global w
        w = self.canvas.winfo_width()
        global h
        global row_num
        global column_num
        h = self.canvas.winfo_height()
        self.canvas.delete('grid_line')
        self.canvas.delete('target')
        self.canvas.delete('factnode')
        self.canvas.delete('connection')
        # self.canvas.delete('grid_line')

        green = tk.PhotoImage("level_editor/green.png")
        self.canvas.create_image(40, 40, image=green)

        for i in range(0, w, int(w / rows)):
            self.canvas.create_line(transform_world_to_screen([i, 0])[0], transform_world_to_screen([i, 0])[1], transform_world_to_screen([i, h])[0], transform_world_to_screen([i, h])[1], tag='grid_line')

        for i in range(0, h, int(h / columns)):
            self.canvas.create_line(transform_world_to_screen([0, i])[0], transform_world_to_screen([0, i])[1], transform_world_to_screen([w, i])[0], transform_world_to_screen([w, i])[1], tag='grid_line')

        # T ODO: add all objects <-- need brian's help for this
        # target_img = tk.PhotoImage(file=str(cwd) + "/green.png")
        # target_img = target_img.subsample(4,4)
        # node_img = tk.PhotoImage(file=str(cwd) + "/node.png")
        # node_img = target_img.subsample(4,4)
        for target in self.level.targets:
            self.canvas.create_text(
                transform_world_to_screen((target.pos[0] * w / row_num + w / 2, 0 * h / column_num + h / 2))[0],
                transform_world_to_screen((target.pos[1] * w / row_num + w / 2, 0 * h / column_num + h / 2))[1],
                font="Arial", text=target.name, tag='target')
            #     image=target_img)
            for factnode in target.factnodes:
                if factnode.pos == selected_node:
                    self.canvas.create_text(
                        transform_world_to_screen((factnode.pos[0] * w / row_num + w / (2 * row_num), factnode.pos[1] * h / column_num + h / (2 * column_num)))[0],
                        transform_world_to_screen((factnode.pos[0] * w / row_num + w / (2 * row_num), factnode.pos[1] * h / column_num + h / (2 * column_num)))[1], 
                        font="Times", text=factnode.name, tag='factnode')
                else:
                    #print(str(factnode.pos))
                    self.canvas.create_text(
                        transform_world_to_screen((factnode.pos[0] * w / row_num + w / (2 * row_num),factnode.pos[1] * h / column_num + h / (2 * column_num)))[0],
                        transform_world_to_screen((factnode.pos[0] * w / row_num + w / (2 * row_num),factnode.pos[1] * h / column_num + h / (2 * column_num)))[1],
                        font="Arial", text=factnode.name, tag='factnode')
                    # image=node_img)
            # T ODO: add thickened lines to indicate a connection
            for factnode in target.factnodes:
                for line_tile in factnode.connection_to_parent:
                    self.canvas.create_text(
                        transform_world_to_screen((line_tile[0] * w / row_num + w / (2 * row_num), line_tile[1] * h / column_num + h / (2 * column_num)))[0],
                        transform_world_to_screen((line_tile[0] * w / row_num + w / (2 * row_num), line_tile[1] * h / column_num + h / (2 * column_num)))[1],
                        font="Arial", text='+', tag='connection')

    """
    def draw_object(self, event, row = 2, column = 2):
        w = self.canvas.winfo_width()
        h = self.canvas.winfo_height()
        row = (event.x - event.x%(w/row_num))/row_num
        column = (event.y - event.y%(h/column_num))/column_num
        self.canvas.create_text((row*w)/row_num - (w/(row_num*2))),(column*h)/column_num - (h/(column_num*2)), font = "WingDings", text = 'T', tag = 'target')
    """

    def open_new_menu(self, event):
        global w
        w = self.canvas.winfo_width()
        global h
        global grid
        global column_num
        global row_num
        global mode
        global menu_open
        global selected_node

        h = self.canvas.winfo_height()
        # TODO: open a different menu depending on the object that was right clicked on. This should be doable since 1view have the model
        window = tk.Tk()
        if mode == target_selected:
            window.title("Target editing menu")
            window.geometry('900x600')
            window.configure(background="grey")


            new_x, new_y = transform_screen_to_world((event.x, event.y))
            row = int(new_x / (w/row_num))
            column = int(new_y / (h/column_num))
            if row == 0 and column == 0:
                pass
            target = self.level.element_at((row, column))


            a = Label(window, text="Name")
            a.grid(row=0, column=0)
            # a.insert(-1, self.level.target.name)
            b = Label(window, text="Neighbors (comma separated, no spaces or it breaks)")
            b.grid(row=1, column=0)
            c = Label(window, text="Suspicion")
            c.grid(row=2, column=0)
            d = Label(window, text="Starting Stress")
            d.grid(row=3, column=0)
            e = Label(window, text="Max Stress")
            e.grid(row=4, column=0)
            f = Label(window, text="World Position X")
            f.grid(row=5, column=0)
            fg = Label(window, text="Combos (comma seperated, no spaces or it breaks)")
            fg.grid(row=6, column=0)
            g = Label(window, text="World Position Y")
            g.grid(row=7, column=0)
            # combos/position/sasbisiong/neighbour/startstress/maxstress
            a1 = Entry(window)
            a1.grid(row=0, column=1)
            #a1.insert(-1, self.level.target.name)
            b1 = Entry(window)
            b1.grid(row=1, column=1)
            #b1.insert(-1, ','.join([neighbor for neighbor in self.level.target.neighbors]))
            c1 = Entry(window)
            c1.grid(row=2, column=1)
            #c1.insert(-1, str(self.level.target.suspicion))
            d1 = Entry(window)
            d1.grid(row=3, column=1)
            #d1.insert(-1, str(self.level.target.starting_stress))
            e1 = Entry(window)
            e1.grid(row=4, column=1)
            #e1.insert(-1, str(self.level.target.max_stress))
            f1 = Entry(window)
            f1.grid(row=5, column=1)
            #f1.insert(-1, str(self.level.target.pos[0]))
            fg1 = ScrolledText(window)
            fg1.grid(row=6, column=1)
            #fg1.insert(-1, str(self.level.target.combos))
            g1 = Entry(window)
            g1.grid(row=7, column=1)
            #g1.insert(-1, str(self.level.target.pos[1]))

            if target != None:
                a1.insert(-1, target.name)
                b1.insert(-1, ','.join([neighbor for neighbor in target.neighbors]))
                c1.insert(-1, str(target.suspicion))
                d1.insert(-1, str(target.starting_stress))
                e1.insert(-1, str(target.max_stress))
                f1.insert(-1, str(target.pos[0]))
                fg1.insert(-1, str(target.combos))
                g1.insert(-1, str(target.pos[1]))


            def submitted():
                # pass shit into controwler
                #     def save_target(self, name, neighbours, susbision, sturess, maxitress, warudoposX, warudoposY, kangbous):
                self.controwler.save_target(a1.get(), b1.get(), c1.get(), d1.get(), e1.get(), f1.get(), g1.get(), fg1.get("1.0", "end"))
                self.canvas.focus_set()
                global menu_open
                menu_open = False

            Button(window, text="Submit", command=submitted).grid(row=8, column=0)
        elif mode == node_selected:
            # if not menu_open:
            menu_open = True
            node = None
            # row = (event.x - event.x % (w / row_num)) / row_num
            # column = (event.y - event.y % (h / column_num)) / column_num
            new_x, new_y = transform_screen_to_world((event.x, event.y))
            row = int(new_x / (w/row_num))
            column = int(new_y / (h/column_num))
            #print('r,c = ' + str(row) + ', ' + str(column))
            if row == 0 and column == 0:
                pass
            # if self.level.element_at((row, column)) == None:
            #     self.controwler.add_object(event)
            node = self.level.element_at((row, column))
            if node:
                selected_node = (row, column)
            else:
                selected_node = None
            # for f in self.level.target.factnodes:
            #         if f.pos[0] == row and f.pos[1] == column:
            #             node = f

            window.title("Node editing menu")
            window.geometry('900x600')
            window.configure(background="grey")
            a = Label(window, text="Name")
            a.grid(row=0, column=0)
            # a.insert(-1, self.level.target.name)
            b = Label(window, text="Title")
            b.grid(row=1, column=0)
            c = Label(window, text="World Position X")
            c.grid(row=2, column=0)
            c11 = Label(window, text="World Position Y")
            c11.grid(row=3, column=0)
            d = Label(window, text="Player Stress Damage")
            d.grid(row=4, column=0)
            e = Label(window, text="Stress Damage")
            e.grid(row=5, column=0)
            f = Label(window, text="Summary")
            f.grid(row=6, column=0)
            g = Label(window, text="Contents")
            g.grid(row=7, column=0)
            hh = Label(window, text="Locked (T/F)")
            hh.grid(row=8, column=0)
            i = Label(window, text = "fact belongs to: ")
            i.grid(row = 9, column = 0)
            # Name/Title/World Position/Player Stress Damage/Stress Damage/Summary/Contents/Locked
            a1 = Entry(window)
            a1.grid(row=0, column=1)
            b1 = Entry(window)
            b1.grid(row=1, column=1)
            c1 = Entry(window)
            c1.grid(row=2, column=1)
            c111 = Entry(window)
            c111.grid(row=3, column=1)
            d1 = Entry(window)
            d1.grid(row=4, column=1)
            e1 = Entry(window)
            e1.grid(row=5, column=1)
            f1 = ScrolledText(window)
            f1.grid(row=6, column=1)
            g1 = Entry(window)
            g1.grid(row=7, column=1)
            h1 = Entry(window)
            h1.grid(row=8, column=1)
            choice = tk.StringVar()
            i1 = OptionMenu(window, choice, self.level.targets)
            i1.grid(row = 9, column = 1)

            if node != None:
                a1.insert(-1, node.name)
                b1.insert(-1, node.title)
                # c1.insert(-1, str(node.pos[0]))
                # c111.insert(-1, str(node.pos[1]))
                d1.insert(-1, node.player_stress_dam)
                e1.insert(-1, node.stress_dam)
                f1.insert(-1, node.summary)
                g1.insert(-1, node.contents)
                h1.insert(-1, str(node.locked))

            def submitted():
                # pass shit into controwler
                self.controwler.save_node("Joe Doe", a1.get(), b1.get(), row, column, d1.get(), e1.get(), f1.get("1.0"), g1.get(), h1.get(), choice.get())
                global menu_open
                menu_open = False
                self.canvas.focus_set()

            Button(window, text="Submit", command=submitted).grid(row=10, column=0)
            # else: # menu open
            #     self.canvas.focus_set()
            #     row = int(event.x / (w/row_num))
            #     column = int(event.y / (h/column_num))
            #     if self.level.element_at((row, column)) == empty_tile:
            #         if (row, column) in self.level.element_at(selected_node).connection_to_parent:
            #             self.level.element_at(selected_node).connection_to_parent.remove((row, column))
            #         else:
            #             self.level.element_at(selected_node).connection_to_parent.add((row, column))
        window.mainloop()


def save(level, out_filename=None):
    if not out_filename:
        out_filename = ''.join(level.target.name.split(' '))
    if out_filename[-5:] != '.json':
        out_filename += '.json'
    out_fs = open(out_filename, mode='w')
    out_fs.write(json.dumps(level))
    out_fs.close()

def load(in_filename):
    if in_filename[-5:] != '.json':
        in_filename += '.json'
    in_fs = open(in_filename, mode='r')
    return Level(**json.load(in_fs))

cwd = os.getcwd()

m = Level()
c = Controwler(m)
v = View(c, m)