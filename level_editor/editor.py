# free-him Level Editor
# Yimeng Zeng, Tony Zhang, Brian Zhu

import json
import tkinter as tk
from tkinter import Label, Entry, Button, Checkbutton
import numpy as np
from PIL import ImageTk, Image

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
            # self.lines = set()  # a set with int duples (x, y)
            # self.factnodes = dict()
        else:
            self.name = contents['name']
            self.combos = {(combo['overwritten'], combo['summary'], int(combo['modified_dam']), combo['nodes'])
                           for combo in contents['combos']}
            self.pos = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.suspicion = int(contents['suspicion'])
            self.max_stress = int(contents['max_stress'])
            self.starting_stress = int(contents['starting_stress'])
            self.neighbors = set(contents['neighbors'])
            # self.lines = set(contents['lines'])
            # self.factnodes

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass


class FactNode:
    def __init__(self, pos=(0, 0), contents=None):
        '''
            contents is a dictionary containing factnode data
        '''
        if contents == None:
            self.name = ''
            self.title = ''
            self.position = pos
            self.children = set()
            # self.parent = None
            self.connection_to_parent = set()
            self.player_stress_dam = 0
            self.stress_dam = 0
            self.summary = ''
            self.contents = ''
            self.locked = True
        else:
            self.name = contents['name']
            self.title = contents['title']
            self.position = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.children = {FactNode(contents=factnode) for factnode in contents['children']}
            # self.parent =
            self.connection_to_parent = {(int(con_route[0]), int(con_route[1])) for con_route in contents['connection_to_parent']}
            self.player_stress_dam = int(contents['player_stress_dam'])
            self.stress_dam = contents['stress_dam']
            self.summary = contents['summary']
            self.contents = contents['contents']
            self.locked = bool(contents['locked'])

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass


class Level:
    def __init__(self, file=None):
        if file == None:
            self.target = Target()
            self.factnodes = set()
        else:
            jason = json.load(file)
            self.target = Target(contents=jason)
            self.factnodes = {FactNode(contents=cont) for cont in jason['factnodes']}
        self.active_element = None
        self.drawing = False  # self.drawing if drawing a FactNode path
        # self.view = None

    def add_view(self, view):
        self.view = view

    def element_at(self, pos):
        pos = (int(pos[0]), int(pos[1]))
        if pos == 0:
            return self.target
        else:
            for factnode in self.factnodes:
                if factnode.position == pos:
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

        print(str(w) + ', ' + str(h))

        # row = (event.x - event.x % (w / row_num)) / row_num
        # column = (event.y - event.y % (int(h) / column_num)) / column_num
        row = int(event.x / (w/row_num))
        column = int(event.y / (h/column_num))

        if mode == node_selected:
            if grid[row][column] == empty_tile:
                self.level.factnodes.add(FactNode(pos=(row, column)))
            # if menu_open:
            #     if grid[row][column] == empty_tile:
            #         self.level.factnodes.add(FactNode(pos=(row, column)))
            # elif selected_node:
            #     if (row,col) in self.level.element_at((selected_node)).connection_to_parent:
            #         self.level.element_at((selected_node)).connection_to_parent.remove((row,col))
            #     else:
            #         self.level.element_at((selected_node)).connection_to_parent.add((row,col))
        elif mode == deletion:
            if grid[row][column] == node_tile:
                for f in self.level.factnodes:
                    if f.pos[0] == row and f.pos[1] == column:
                        self.level.factnodes.remove(f)
            if grid[row][column] == line_tile:
                for l in self.level.target.lines:
                    if l.pos[0] == row and l.pos[1] == column:
                        self.level.target.lines.remove(l)
        elif mode == draw_line:
            if grid[row][column] == empty_tile:
                self.level.target.lines.add((row, column))

    def save_target(self, name, neighbours, susbision, sturess, maxitress, warudoposX, warudoposY):
        self.level.target.name = name
        self.level.target.neighbors = set(neighbours.split(","))
        self.level.target.suspicion = int(susbision)
        self.level.target.starting_stress = int(sturess)
        self.level.target.max_stress = int(maxitress)
        self.level.target.pos = (int(warudoposX), int(warudoposY))

    # Name/Title/World PositionX/Y/Player Stress Damage/Stress Damage/Summary/Contents/Locked
    def save_node(self, name, taitoru, warudoposX, warudoposY, platresmg, tresmg, Summary, Contents, Locked):
        tempdict = {
            'name': name,
            'title': taitoru,
            'pos': (int(warudoposX), int(warudoposY)),
            'children': set(),
            'player_stress_dam': int(platresmg),
            'stress_dam': int(tresmg),
            'summary': Summary,
            'contents': Contents,
            'locked': (Locked == 'T')
        }
        noude = FactNode(pos=(int(warudoposX), int(warudoposY)), contents=tempdict)
        self.level.factnodes.add(noude)


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
        self.canvas.bind('<Key>', self.change_mode)
        self.canvas.focus_set()
        self.root.mainloop()

    def change_mode(self, event=None):
        self.canvas.focus_set()
        global mode
        if event.char in {'1', '2', '3', '4'}:
            if mode == int(event.char):
                mode = 0
            else:
                mode = int(event.char)
        else:
            mode = 0

    def draw_everything(self, event=None, rows=row_num, columns=column_num):
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

        for i in range(0, w, int(w / rows)):
            self.canvas.create_line([i, 0], [i, h], tag='grid_line')

        for i in range(0, h, int(h / columns)):
            self.canvas.create_line([0, i], [w, i], tag='grid_line')

        # T ODO: add all objects <-- need brian's help for this
        target_img = tk.PhotoImage(file="green.png")
        target_img = target_img.subsample(4,4)
        node_img = tk.PhotoImage(file="node.png")
        node_img = target_img.subsample(4,4)

        self.canvas.create_image(
            0 * w / row_num + w / 2,
            0 * h / column_num + h / 2,
            #font="Arial", text=self.level.target.name, tag='target')
            image=target_img)
        for factnode in self.level.factnodes:
            self.canvas.create_image(
                factnode.position[0] * w / row_num + w / (2 * row_num),
                factnode.position[1] * h / column_num + h / (2 * column_num),
                image=node_img)
        # T ODO: add thickened lines to indicate a connection
        for line_tile in self.level.target.lines:
            self.canvas.create_text(
                line_tile.pos[0] * w / row_num + w / (2 * row_num),
                line_tile.pos[1] * h / column_num + h / (2 * column_num),
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
            window.geometry('600x600')
            window.configure(background="grey")
            a = Label(window, text="Name")
            a.grid(row=0, column=0)
            # a.insert(-1, self.level.target.name)
            b = Label(window, text="Neighbors (comma separated)")
            b.grid(row=1, column=0)
            c = Label(window, text="Suspicion")
            c.grid(row=2, column=0)
            d = Label(window, text="Starting Stress")
            d.grid(row=3, column=0)
            e = Label(window, text="Max Stress")
            e.grid(row=4, column=0)
            f = Label(window, text="World Position X")
            f.grid(row=5, column=0)
            g = Label(window, text="World Position Y")
            g.grid(row=6, column=0)
            # combos/position/sasbisiong/neighbour/startstress/maxstress
            a1 = Entry(window)
            a1.grid(row=0, column=1)
            a1.insert(-1, self.level.target.name)
            b1 = Entry(window)
            b1.grid(row=1, column=1)
            b1.insert(-1, ','.join([neighbor for neighbor in self.level.target.neighbors]))
            c1 = Entry(window)
            c1.grid(row=2, column=1)
            c1.insert(-1, str(self.level.target.suspicion))
            d1 = Entry(window)
            d1.grid(row=3, column=1)
            d1.insert(-1, str(self.level.target.starting_stress))
            e1 = Entry(window)
            e1.grid(row=4, column=1)
            e1.insert(-1, str(self.level.target.max_stress))
            f1 = Entry(window)
            f1.grid(row=5, column=1)
            f1.insert(-1, str(self.level.target.pos[0]))
            g1 = Entry(window)
            g1.grid(row=6, column=1)
            g1.insert(-1, str(self.level.target.pos[1]))

            def submitted():
                # pass shit into controwler
                self.controwler.save_target(a1.get(), b1.get(), c1.get(), d1.get(), e1.get(), f1.get(), g1.get())
                # menu_open = False

            Button(window, text="Submit", command=submitted).grid(row=7, column=0)
        elif mode == node_selected:
            if not menu_open:
                menu_open = True
                # node = None
                # row = (event.x - event.x % (w / row_num)) / row_num
                # column = (event.y - event.y % (h / column_num)) / column_num
                row = int(event.x / (w/row_num))
                column = int(event.y / (h/column_num))
                # if self.level.element_at((row, column)) == None:
                #     self.controwler.add_object(event)
                node = self.level.element_at((row, column))
                if node:
                    selected_node = (row, column)
                else:
                    selected_node = None
                # for f in self.level.factnodes:
                #         if f.pos[0] == row and f.pos[1] == column:
                #             node = f

                window.title("Node editing menu")
                window.geometry('600x600')
                window.configure(background="grey")
                a = Label(window, text="Name")
                a.grid(row=0, column=0)
                # a.insert(-1, self.level.target.name)
                b = Label(window, text="Title")
                b.grid(row=1, column=0)
                # c = Label(window, text="World Position X")
                # c.grid(row=2, column=0)
                # c11 = Label(window, text="World Position Y")
                # c11.grid(row=3, column=0)
                d = Label(window, text="Player Stress Damage")
                d.grid(row=4, column=0)
                e = Label(window, text="Stress Damage")
                e.grid(row=5, column=0)
                f = Label(window, text="Summary")
                f.grid(row=6, column=0)
                g = Label(window, text="Contents")
                g.grid(row=7, column=0)
                h = Label(window, text="Locked (T/F)")
                h.grid(row=8, column=0)
                # Name/Title/World Position/Player Stress Damage/Stress Damage/Summary/Contents/Locked
                a1 = Entry(window)
                a1.grid(row=0, column=1)
                b1 = Entry(window)
                b1.grid(row=1, column=1)
                # c1 = Entry(window)
                # c1.grid(row=2, column=1)
                # c111 = Entry(window)
                # c111.grid(row=3, column=1)
                d1 = Entry(window)
                d1.grid(row=4, column=1)
                e1 = Entry(window)
                e1.grid(row=5, column=1)
                f1 = Entry(window)
                f1.grid(row=6, column=1)
                g1 = Entry(window)
                g1.grid(row=7, column=1)
                h1 = Entry(window)
                h1.grid(row=8, column=1)

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
                    self.controwler.save_node(a1.get(), b1.get(), row, column, d1.get(), e1.get(), f1.get(),
                                            g1.get(), h1.get())
                    menu_open = False

                Button(window, text="Submit", command=submitted).grid(row=9, column=0)
            else: # menu open
                row = int(event.x / (w/row_num))
                column = int(event.y / (h/column_num))
                if self.level.element_at((row, column)) == empty_tile:
                    if (row, column) in self.level.element_at(selected_node).connection_to_parent:
                        self.level.element_at(selected_node).connection_to_parent.remove((row, column))
                    else:
                        self.level.element_at(selected_node).connection_to_parent.add((row, column))
        window.mainloop()


def save(level, out_filename=None):
    if not out_filename:
        out_filename = ''.join(level.target.name.split(' '))
    if out_filename[-5:] != '.json':
        out_filename += '.json'
    out_fs = open(out_filename, mode='w')
    out_fs.write(json.dumps(level))
    out_fs.close()


m = Level()
c = Controwler(m)
v = View(c, m)