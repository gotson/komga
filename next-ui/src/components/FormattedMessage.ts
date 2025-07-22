import { defineComponent, h } from 'vue'
import { type MessageDescriptor, useIntl } from 'vue-intl'

// from: https://github.com/formatjs/formatjs/discussions/3961
export default defineComponent({
  props: {
    messageDescriptor: Object as PropType<MessageDescriptor>,
    tag: String,
    values: Object as PropType<Record<string, string | number | boolean | Date | undefined | null>>,
  },
  setup(props, context) {
    const { messageDescriptor, tag, values = {} } = props
    if (!messageDescriptor) return () => ''

    const intl = useIntl()
    const slotNames = Object.keys(context.slots)

    const v = {
      ...values,
      ...slotNames.reduce((slots: Record<string, unknown>, name) => {
        slots[name] = (content: unknown) => context.slots[name]!(() => content)
        return slots
      }, {}),
    } as Record<string, string | number | boolean | Date | undefined | null>
    const message = intl.formatMessage(messageDescriptor, v)

    return () => (tag || Array.isArray(message) ? h(tag || 'div', message) : message)
  },
})
