class Token:
    def __init__(self, src_address, src_port, dest_address, dest_port, text, category):
        self.src_address = src_address
        self.src_port = src_port
        self.dest_address = dest_address
        self.dest_port = dest_port
        self.text = text
        self.category = category

    def __str__(self):
        return ("TOKEN {} \n FROM {}:{} TO {}:{} \n TEXT: {}"
                .format(self.category, self.src_address, self.src_port, self.dest_address, self.dest_port, self.text))

    def is_free(self):
        return not self.text
